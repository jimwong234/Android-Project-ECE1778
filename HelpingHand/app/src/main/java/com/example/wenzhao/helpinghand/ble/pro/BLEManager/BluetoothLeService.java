package com.example.wenzhao.helpinghand.ble.pro.BLEManager;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
	static final String TAG = "BluetoothLeService";

	public final static String ACTION_GATT_CONNECTED = "com.example.ti.ble.common.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.ti.ble.common.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED1 = "com.example.ti.ble.common.ACTION_GATT_SERVICES_DISCOVERED1";
	public final static String ACTION_GATT_SERVICES_DISCOVERED2 = "com.example.ti.ble.common.ACTION_GATT_SERVICES_DISCOVERED2";
	public final static String ACTION_DATA_READ = "com.example.ti.ble.common.ACTION_DATA_READ";
	public final static String ACTION_DATA_NOTIFY = "com.example.ti.ble.common.ACTION_DATA_NOTIFY";
	public final static String ACTION_DATA_NOTIFY1 = "com.example.ti.ble.common.ACTION_DATA_NOTIFY1";
	public final static String ACTION_DATA_WRITE = "com.example.ti.ble.common.ACTION_DATA_WRITE";
	public final static String EXTRA_DATA = "com.example.ti.ble.common.EXTRA_DATA";
	public final static String EXTRA_UUID = "com.example.ti.ble.common.EXTRA_UUID";
	public final static String EXTRA_STATUS = "com.example.ti.ble.common.EXTRA_STATUS";
	public final static String EXTRA_ADDRESS = "com.example.ti.ble.common.EXTRA_ADDRESS";
	public final static int GATT_TIMEOUT = 150;
	public int cnum = 0;

	public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	// BLE
	private BluetoothManager mBluetoothManager = null;
	private BluetoothAdapter mBtAdapter = null;
	private BluetoothGatt mBluetoothGatt = null;
	private ArrayList<BluetoothGatt> connectionQueue = new ArrayList<BluetoothGatt>();
	private ArrayList<BluetoothDevice> connectedDevice = new ArrayList<BluetoothDevice>();

	private static BluetoothLeService mThis = null;
	private String mBluetoothDeviceAddress;

	public Timer disconnectionTimer;
	private final Lock lock = new ReentrantLock();

	private volatile boolean blocking = false;
	private volatile int lastGattStatus = 0; //Success

	private volatile bleRequest curBleRequest = null;



	public enum bleRequestOperation {
		wrBlocking,
		wr,
		rdBlocking,
		rd,
		nsBlocking,
	}

	public enum bleRequestStatus {
		not_queued,
		queued,
		processing,
		timeout,
		done,
		no_such_request,
		failed,
	}

	public class bleRequest {
		public int id;
		public BluetoothGattCharacteristic characteristic;
		public BluetoothGatt gatt;
		public bleRequestOperation operation;
		public volatile bleRequestStatus status;
		public int timeout;
		public int curTimeout;
		public boolean notifyenable;
	}

	// Queuing for fast application response.
	private volatile LinkedList<bleRequest> procQueue;
	private volatile LinkedList<bleRequest> nonBlockQueue;

	//

	/**
	 * GATT client callbacks
	 */
	private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {
			BluetoothDevice device = gatt.getDevice();
			String address = device.getAddress();
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				broadcastUpdate(ACTION_GATT_CONNECTED, address, status);
				gatt.discoverServices();
			}
			try {
				switch (newState) {
					case BluetoothProfile.STATE_CONNECTED:
						broadcastUpdate(ACTION_GATT_CONNECTED, address, status);
						break;
					case BluetoothProfile.STATE_DISCONNECTED:
						broadcastUpdate(ACTION_GATT_DISCONNECTED, address, status);
						break;
					default:
						break;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

			BluetoothDevice device = gatt.getDevice();
			broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED1, device.getAddress(),
					status);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_NOTIFY, characteristic,
					BluetoothGatt.GATT_SUCCESS);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {
			if (blocking)unlockBlockingThread(status);
			if (nonBlockQueue.size() > 0) {
				lock.lock();
				for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
					bleRequest req = nonBlockQueue.get(ii);
					if (req.characteristic == characteristic) {
						req.status = bleRequestStatus.done;
						nonBlockQueue.remove(ii);
						break;
					}
				}
				lock.unlock();
			}
			broadcastUpdate(ACTION_DATA_READ, characteristic, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {
			if (blocking)unlockBlockingThread(status);
			if (nonBlockQueue.size() > 0) {
				lock.lock();
				for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
					bleRequest req = nonBlockQueue.get(ii);
					if (req.characteristic == characteristic) {
						req.status = bleRequestStatus.done;
						nonBlockQueue.remove(ii);
						break;
					}
				}
				lock.unlock();
			}
			broadcastUpdate(ACTION_DATA_WRITE, characteristic, status);
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
									 BluetoothGattDescriptor descriptor, int status) {
			if (blocking)unlockBlockingThread(status);
			unlockBlockingThread(status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
									  BluetoothGattDescriptor descriptor, int status) {
			if (blocking)unlockBlockingThread(status);
		}
	};
	private BluetoothGattCallback mGattCallbacks1 = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {
			BluetoothDevice device = gatt.getDevice();
			String address = device.getAddress();
			if(status == BluetoothGatt.GATT_SUCCESS)
			{
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					broadcastUpdate(ACTION_GATT_CONNECTED, address, status);
					gatt.discoverServices();
				}

			}
			try {
				switch (newState) {
					case BluetoothProfile.STATE_CONNECTED:
						broadcastUpdate(ACTION_GATT_CONNECTED, address, status);
						break;
					case BluetoothProfile.STATE_DISCONNECTED:
						broadcastUpdate(ACTION_GATT_DISCONNECTED, address, status);
						break;
					default:
						break;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			BluetoothDevice device = gatt.getDevice();
			broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED2, device.getAddress(),
					status);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_NOTIFY1, characteristic,
					BluetoothGatt.GATT_SUCCESS);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {
			if (blocking)unlockBlockingThread(status);
			if (nonBlockQueue.size() > 0) {
				lock.lock();
				for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
					bleRequest req = nonBlockQueue.get(ii);
					if (req.characteristic == characteristic) {
						req.status = bleRequestStatus.done;
						nonBlockQueue.remove(ii);
						break;
					}
				}
				lock.unlock();
			}
			broadcastUpdate(ACTION_DATA_READ, characteristic, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {
			if (blocking)unlockBlockingThread(status);
			if (nonBlockQueue.size() > 0) {
				lock.lock();
				for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
					bleRequest req = nonBlockQueue.get(ii);
					if (req.characteristic == characteristic) {
						req.status = bleRequestStatus.done;
						nonBlockQueue.remove(ii);
						break;
					}
				}
				lock.unlock();
			}
			broadcastUpdate(ACTION_DATA_WRITE, characteristic, status);
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
									 BluetoothGattDescriptor descriptor, int status) {
			if (blocking)unlockBlockingThread(status);
			unlockBlockingThread(status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
									  BluetoothGattDescriptor descriptor, int status) {
			if (blocking)unlockBlockingThread(status);
		}
	};

	private void unlockBlockingThread(int status) {
		this.lastGattStatus = status;
		this.blocking = false;
	}

	private void broadcastUpdate(final String action, final String address,
								 final int status) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_ADDRESS, address);
		intent.putExtra(EXTRA_STATUS, status);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
								 final BluetoothGattCharacteristic characteristic, final int status) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
		intent.putExtra(EXTRA_DATA, characteristic.getValue());
		intent.putExtra(EXTRA_STATUS, status);
		sendBroadcast(intent);
	}

	public boolean checkGatt() {
		if (mBtAdapter == null) {
			return false;
		}
		if (this.blocking) {
			return false;
		}
		return true;
	}

	private boolean checkGatt(BluetoothGatt bluetoothGatt) {
		if (!connectionQueue.isEmpty()) {
			for(BluetoothGatt btg:connectionQueue){
				if(btg.equals(bluetoothGatt)){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Manage the BLE service
	 */
	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	private final IBinder binder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter through
		// BluetoothManager.
		mThis = this;
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				// Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBtAdapter = mBluetoothManager.getAdapter();
		if (mBtAdapter == null) {
			// Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		procQueue = new LinkedList<bleRequest>();
		nonBlockQueue = new LinkedList<bleRequest>();


		Thread queueThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					executeQueue();
					try {
						Thread.sleep(0, 100000);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};

		queueThread.start();
		return true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Log.i(TAG, "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		this.initialize();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (connectionQueue.size() != 0) {
			for(BluetoothGatt bluetoothGatt:connectionQueue){
				bluetoothGatt.close();
				bluetoothGatt = null;
			}
			connectionQueue = null;
		}
		connectedDevice = null;

	}

	//
	// GATT API
	//

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic
	 *          The characteristic to read from.
	 */

	public int writeCharacteristic(
			BluetoothGattCharacteristic characteristic,BluetoothGatt gatt , byte b) {
		byte[] val = new byte[1];
		val[0] = b;
		characteristic.setValue(val);

		bleRequest req = new bleRequest();
		req.status = bleRequestStatus.not_queued;
		req.gatt = gatt;
		req.characteristic = characteristic;
		req.operation = bleRequestOperation.wrBlocking;
		addRequestToQueue(req);
		boolean finished = false;
		while (!finished) {
			bleRequestStatus stat = pollForStatusofRequest(req);
			if (stat == bleRequestStatus.done) {
				finished = true;
				return 0;
			}
			else if (stat == bleRequestStatus.timeout) {
				finished = true;
				return -3;
			}
		}
		return -2;
	}
	public int writeCharacteristic(
			BluetoothGattCharacteristic characteristic, BluetoothGatt gatt,byte[] b) {
		characteristic.setValue(b);
		bleRequest req = new bleRequest();
		req.gatt = gatt;
		req.status = bleRequestStatus.not_queued;
		req.characteristic = characteristic;
		req.operation = bleRequestOperation.wrBlocking;

		addRequestToQueue(req);
		boolean finished = false;
		while (!finished) {
			bleRequestStatus stat = pollForStatusofRequest(req);
			if (stat == bleRequestStatus.done) {
				finished = true;
				return 0;
			}
			else if (stat == bleRequestStatus.timeout) {
				finished = true;
				return -3;
			}
		}
		return -2;
	}

	/**
	 * Retrieves the number of GATT services on the connected device. This should
	 * be invoked only after {@code BluetoothGatt#discoverServices()} completes
	 * successfully.
	 *
	 * @return A {@code integer} number of supported services.
	 */
	public int getNumServices() {
		if (mBluetoothGatt == null)
			return 0;

		return mBluetoothGatt.getServices().size();
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}


	public int setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic,BluetoothGatt gatt, boolean enable) {
		bleRequest req = new bleRequest();
		req.status = bleRequestStatus.not_queued;
		req.gatt =gatt;
		req.characteristic = characteristic;
		req.operation = bleRequestOperation.nsBlocking;
		req.notifyenable = enable;
		addRequestToQueue(req);
		boolean finished = false;
		while (!finished) {
			bleRequestStatus stat = pollForStatusofRequest(req);
			if (stat == bleRequestStatus.done) {
				finished = true;
				return 0;
			}
			else if (stat == bleRequestStatus.timeout) {
				finished = true;
				return -3;
			}
		}
		return -2;
	}

	public void connect(final String address) {
		BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
		if (cnum == 0){
			BluetoothGatt bluetoothGatt = device.connectGatt(this, false, mGattCallbacks);
			mBluetoothGatt = bluetoothGatt;
			connectionQueue.add(bluetoothGatt);
			connectedDevice.add(bluetoothGatt.getDevice());
			cnum++;
		}else if (cnum == 1){
			BluetoothGatt bluetoothGatt = device.connectGatt(this, false, mGattCallbacks1);
			mBluetoothGatt = bluetoothGatt;
			connectionQueue.add(bluetoothGatt);
			connectedDevice.add(bluetoothGatt.getDevice());
			cnum = 0;
		}
	}

	public void disconnect(String address) {
		final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
		int connectionState = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
		if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
			for(BluetoothGatt bluetoothGatt:connectionQueue){
				bluetoothGatt.disconnect();
			}
			connectionQueue.clear();
			connectedDevice.clear();
		}
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (connectionQueue != null) {
			// Log.i(TAG, "close");
			for(BluetoothGatt bluetoothGatt:connectionQueue){
				bluetoothGatt.close();
			}
			connectionQueue =null;
		}
	}

	public int numConnectedDevices() {
		int n = 0;

		if (mBluetoothGatt != null) {
			List<BluetoothDevice> devList;
			devList = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
			n = devList.size();
		}
		return n;
	}
	public static ArrayList<BluetoothDevice> getDevice() {
		return mThis.connectedDevice;
	}
	//
	// Utility functions
	//
	public static ArrayList<BluetoothGatt> getBtGatt() {
		return mThis.connectionQueue;
	}

	public static BluetoothManager getBtManager() {
		return mThis.mBluetoothManager;
	}

	public static BluetoothLeService getInstance() {
		return mThis;
	}

	public void waitIdle(int timeout) {
		while (timeout-- > 0) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean addRequestToQueue(bleRequest req) {
		lock.lock();
		if (procQueue.peekLast() != null) {
			req.id = procQueue.peek().id++;
		}
		else {
			req.id = 0;
			procQueue.add(req);
		}
		lock.unlock();
		return true;
	}

	public bleRequestStatus pollForStatusofRequest(bleRequest req) {
		lock.lock();
		if (req == curBleRequest) {
			bleRequestStatus stat = curBleRequest.status;
			if (stat == bleRequestStatus.done) {
				curBleRequest = null;
			}
			if (stat == bleRequestStatus.timeout) {
				curBleRequest = null;
			}
			lock.unlock();
			return stat;
		}
		else {
			lock.unlock();
			return bleRequestStatus.no_such_request;
		}
	}
	private void executeQueue() {
		// Everything here is done on the queue
		lock.lock();
		if (curBleRequest != null) {
			try {
				curBleRequest.curTimeout++;
				if (curBleRequest.curTimeout > GATT_TIMEOUT) {
					curBleRequest.status = bleRequestStatus.timeout;
					curBleRequest = null;
				}
				Thread.sleep(10, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			lock.unlock();
			return;
		}
		if (procQueue == null) {
			lock.unlock();
			return;
		}
		if (procQueue.size() == 0) {
			lock.unlock();
			return;
		}
		bleRequest procReq = procQueue.removeFirst();

		switch (procReq.operation) {
			case rd:
				//Read, do non blocking read
				break;
			case rdBlocking:
				//Normal (blocking) read
				if (procReq.timeout == 0) {
					procReq.timeout = GATT_TIMEOUT;
				}
				procReq.curTimeout = 0;
				curBleRequest = procReq;
				int stat = sendBlockingReadRequest(procReq);
				if (stat == -2) {
					lock.unlock();
					return;
				}
				break;
			case wr:
				//Write, do non blocking write (Ex: OAD)
				nonBlockQueue.add(procReq);
				sendNonBlockingWriteRequest(procReq);
				break;
			case wrBlocking:
				//Normal (blocking) write
				if (procReq.timeout == 0) {
					procReq.timeout = GATT_TIMEOUT;
				}
				curBleRequest = procReq;
				stat = sendBlockingWriteRequest(procReq);
				if (stat == -2) {
					lock.unlock();
					return;
				}
				break;
			case nsBlocking:
				if (procReq.timeout == 0) {
					procReq.timeout = GATT_TIMEOUT;
				}
				curBleRequest = procReq;
				stat = sendBlockingNotifySetting(procReq);
				if (stat == -2) {
					lock.unlock();
					return;
				}
				break;
			default:
				break;

		}
		lock.unlock();
	}

	public int sendNonBlockingWriteRequest(bleRequest request) {
		request.status = bleRequestStatus.processing;
		if (!checkGatt()) {
			request.status = bleRequestStatus.failed;
			return -2;
		}
		BluetoothGatt bluetoothGatt = request.gatt;
		bluetoothGatt.writeCharacteristic(request.characteristic);

		return 0;
	}

	public int sendBlockingReadRequest(bleRequest request) {
		request.status = bleRequestStatus.processing;
		int timeout = 0;
		if (!checkGatt()) {
			request.status = bleRequestStatus.failed;
			return -2;
		}
		BluetoothGatt bluetoothGatt = request.gatt;
		bluetoothGatt.readCharacteristic(request.characteristic);

		this.blocking = true; // Set read to be blocking
		while (this.blocking) {
			timeout ++;
			waitIdle(1);
			if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
		}
		request.status = bleRequestStatus.done;
		return lastGattStatus;
	}

	public int sendBlockingWriteRequest(bleRequest request) {
		request.status = bleRequestStatus.processing;
		int timeout = 0;
		if (!checkGatt()) {
			request.status = bleRequestStatus.failed;
			return -2;
		}
		BluetoothGatt bluetoothGatt =request.gatt;
		bluetoothGatt.writeCharacteristic(request.characteristic);
		this.blocking = true; // Set read to be blocking
		while (this.blocking) {
			timeout ++;
			waitIdle(1);
			if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
		}
		request.status = bleRequestStatus.done;
		return lastGattStatus;
	}
	public int sendBlockingNotifySetting(bleRequest request) {
		request.status = bleRequestStatus.processing;
		int timeout = 0;
		if (request.characteristic == null) {
			return -1;
		}
		if (!checkGatt())
			return -2;

		BluetoothGatt bluetoothGatt = request.gatt;
		if (bluetoothGatt.setCharacteristicNotification(request.characteristic, request.notifyenable)) {

			BluetoothGattDescriptor clientConfig = request.characteristic
					.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
			if (clientConfig != null) {

				if (request.notifyenable) {
					clientConfig
							.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				} else {
					clientConfig
							.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				}
				bluetoothGatt.writeDescriptor(clientConfig);
				this.blocking = true; // Set read to be blocking
				while (this.blocking) {
					timeout ++;
					waitIdle(1);
					if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
				}
				request.status = bleRequestStatus.done;
				return lastGattStatus;
			}
		}
		return -3; // Set notification to android was wrong ...
	}
}
