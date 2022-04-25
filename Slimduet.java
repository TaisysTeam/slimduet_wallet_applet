/**
	MIT License

	Copyright (c) [2022] [TAISYS TECHNOLOGIES CO., LTD.]

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/

package com.taisys.Slimduet.Applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;
import uicc.toolkit.*;
import uicc.access.FileView;
import uicc.access.UICCSystem;
import uicc.access.UICCException;
import org.globalplatform.*;


public class Slimduet extends Applet implements ToolkitInterface, ToolkitConstants, AppletEvent, MultiSelectable {
	
	private static short appletVersion = (short)0x0001;
	private static byte testVersion = (byte)0xFF;

	// Wallet Command
	private final byte INS_QUERY_CARD_INFOR 		= (byte)0xE0;
	private final byte INS_CREATE_WALLET_USER 	 	= (byte)0xE1;
	private final byte INS_GET_CHILD_PUBLIC_KEY 	= (byte)0xE3;
	private final byte INS_SIGN_TRANSFER 			= (byte)0xE4;
	private final byte INS_SYNCH_WALLET_INFOR 		= (byte)0xE5;
	
	private final static short SW_HD_USER_NO_EXIST 		= (short)0x6982;
	private final static short SW_HD_WALLET_EXIST 		= (short)0x6986;
	private final static short SW_HD_USER_OVERSIZE 		= (short)0x6987;
	private final static short SW_USER_CANCEL			= (short)0x6985;
	private final static short SW_DATA_INVALID 			= (short)0x6A84;
	private final static short SW_SIGN_VERIFYING 		= (short)0x698A;
	private final static short SW_VERIFY_FAILD 			= (short)0x698D;
	private final static short SW_LANGUAGE_NO_EXIST     = (short)0x698C;
	private final static short SW_PIN_LOCK     			= (short)0x698E;
	private final static short SW_PUK_LOCK     			= (short)0x6995;
	private final static short SW_STATUS_NO_ERROR		= (short)0x9000;
	private final static short SW_STK_BUSY				= (short)0x9300;

	public final static short HD_USER_NUM_MAX = 1;
	public final static short HASH_NUM_SIZE = 10;
	public final static short MNEMONIC_SIZE = 12;

	public final static short USER_ID_SIZE = 16;
	public final static short CHAIN_CODE_SIZE = 32;
	public final static short KEY_SECRET_SIZE = 32;
	public final static short HASH_VALUE_SIZE = 32;
	public final static short COMPLETE_USER_PATH_SIZE = 20;
	
	private final static byte [] MNEMONIC_WORD = {'m', 'n', 'e', 'm', 'o', 'n', 'i', 'c'};
	private final static byte [] BITCOIN_SEED = {'B','i','t','c','o','i','n',' ','s','e','e','d'};
	private final static short[] ALPHABE_INDEX = {(short)0x0000, (short)0x04C8, (short)0x08E5, (short)0x0F6F, (short)0x135F, (short)0x16E3, (short)0x1A9D, (short)0x1D49, (short)0x1F89, (short)0x2178, (short)0x222C, (short)0x22E0, (short)0x258C, (short)0x293D, (short)0x2AAE, (short)0x2C9D, (short)0x3141, (short)0x3189, (short)0x355E, (short)0x3E1F, (short)0x4260, (short)0x439B, (short)0x4539, (short)0x47A6, (short)0x47A6, (short)0x47DC, (short)0x47F7};	
	
	// FID
	private final static short FID_DF_BIP = (short)0x7381;
	private final static short FID_EF_CARD_ID = (short)0x1B10;
	private final static short FID_EF_BIP_IP = (short)0x42B1;
	private final static short FID_EF_BIP_Port = (short)0x42B2;
	private final static short FID_DF_COLDWALLET		= (short)0x7382;
	private final static short FID_EF_MENU_MNEMONICWORD = (short)0x1F8A;
	private final static short FID_EF_MenuWording1 = (short)0x1F9A;
	private final static short FID_EF_MenuWording2 = (short)0x1F9B;
	private final static short FID_EF_MenuWording3 = (short)0x1F9C;
	private final static short FID_EF_MenuWording4 = (short)0x1F9D;

	private final static short[] FID_EF_MENU_WORDING = {FID_EF_MenuWording1, FID_EF_MenuWording2, FID_EF_MenuWording3, FID_EF_MenuWording4};
	private final static byte[] DCS = {(byte)0x04, (byte)0x08, (byte)0x08, (byte)0x08};
	
	private byte menuLanguage; // 0 = English ; 1 = Traditional Chinese ; 2 = Simplified Chinese ; 3 = Japanese

	// Device information
	private final static short SIZE_ICCID		= (short)33;	// ICCID
	private final static short SIZE_VER			= (short)65;	// applet version
	private final static short SIZE_CARD_TYPE	= (short)4;		
	private final static short SIZE_RESERVE		= (short)3;		

	private final static short OFFSET_ICCID			= (short)0;
	private final static short OFFSET_VER			= OFFSET_ICCID + SIZE_ICCID;
	private final static short OFFSET_CARD_TYPE		= OFFSET_VER + SIZE_VER;
	private final static short OFFSET_RESERVE		= OFFSET_CARD_TYPE + SIZE_CARD_TYPE;


	private byte[] devInfo;


	// PIN/PUK
	private final static short MAX_PIN_LEN = (short)8;
	private final static short MIN_PIN_LEN = (short)4;
	private final static short MAX_PUK_LEN = (short)8;
	private final static short PIN_ATTEMPT_NUM = (short)5;
	private final static short PUK_ATTEMPT_NUM = (short)6;
	private OwnerPIN pin;
	private OwnerPIN puk;
	private short pinTryCount;
	private short unlockTryCount;
	private boolean isUnBlockedWithPuk = false;

	// BIP Http message
	private final static byte[] checkKeyChange = {(byte)'k', (byte)'e', (byte)'y', (byte)'c', (byte)'h', (byte)'a', (byte)'n', (byte)'g', (byte)'e', (byte)'_', (byte)'o', (byte)'k' };
	private final static byte[] BIP_HTTP_HEAD = {(byte)'P', (byte)'O', (byte)'S', (byte)'T', (byte)' ', (byte)'/', (byte)' ',(byte)'H', (byte)'T', (byte)'T', (byte)'P', (byte)'/',
												 (byte)'1', (byte)'.', (byte)'1', (byte)0x0D, (byte)0x0A, (byte)'H', (byte)'o', (byte)'s', (byte)'t', (byte)':', (byte)' '};
	private final static byte[] BIP_HTTP_CONTENT1 = {(byte)0x0D, (byte)0x0A, (byte)'U', (byte)'s', (byte)'e', (byte)'r', (byte)'-', (byte)'A', (byte)'g', (byte)'e', (byte)'n', (byte)'t',
													(byte)':', (byte)' ', (byte)'t', (byte)'a', (byte)'i', (byte)'s', (byte)'y', (byte)'s', (byte)'_', (byte)'c', (byte)'a', (byte)'r',
													(byte)'d', (byte)'/', (byte)'1', (byte)'.', (byte)'0', (byte)0x0D, (byte)0x0A, (byte)'C', (byte)'o', (byte)'n',(byte)'t', (byte)'e',
													(byte)'n', (byte)'t', (byte)'-', (byte)'L', (byte)'e', (byte)'n', (byte)'g', (byte)'t', (byte)'h', (byte)':'};
	private final static byte[] BIP_HTTP_CONTENT = {(byte)0x0D, (byte)0x0A, (byte)'C', (byte)'o', (byte)'o', (byte)'k', (byte)'i', (byte)'e', (byte)':', (byte)' ', };
	private final static byte[] BIP_HTTP_END = {(byte)0x0D, (byte)0x0A, (byte)0x0D, (byte)0x0A };
	// BIP key
	private final static byte[] MasterSessionKey = {(byte)0x54, (byte)0x68, (byte)0x61, (byte)0x74, (byte)0x73, (byte)0x20, (byte)0x6D, (byte)0x79, (byte)0x20, (byte)0x4B,
														(byte)0x75, (byte)0x6E, (byte)0x67, (byte)0x20, (byte)0x46, (byte)0x75, (byte)0x54, (byte)0x68, (byte)0x61, (byte)0x74,
														(byte)0x73, (byte)0x20, (byte)0x6D, (byte)0x79, (byte)0x20, (byte)0x4B, (byte)0x75, (byte)0x6E, (byte)0x67, (byte)0x20,
														(byte)0x46, (byte)0x75 };


	private boolean isProcessingBIP = false;
	private boolean isBipRspReady = false;
	private boolean isNoService; // Record the previous state service status ,used to decide whether to refresh
	private boolean isBETag = true;
	private short preToken;
	private short currentSerialNum;
	private short channel_data_length;
	private short ipAndPortRecNum;
	private byte channel;
	private byte waitDataDownloadTimes;
	private byte connectiveStatus;	// s = Short connection ; l = long connection ; k = key change
	private byte bipPauseCounter;
	private byte localStatus; // Record current local status , 0 = Normal service ; 1 = limited service ; 2 = No service

											

	// Proactive tag
	private final static byte BEARER_DESCRIPTION_TAG = (byte)0xB5;
	private final static byte BUFFER_SIZE_TAG = (byte)0xB9;
	private final static byte UICC_TERMINAL_TRANSPORT_LEVEL_TAG = (byte)0xBC;
	private final static byte OTHER_DATA_DESTINATION_ADDRESS_TAG = (byte)0xBE;
	private final static byte CHANNEL_DATA_TAG = (byte)0xB6;
	private final static byte CHANNEL_DATA_LENGTH_TAG = (byte)0xB7;
	

	//====Temporary Buffer
	private final static short SIZE_BUFFER_RAM    = (short)256;
	private final static short SIZE_BUFFER_LARGE  = (short)256;
	private final static short SIZE_BUFFER_SMALL  = (short)64;
	private final static short SIZE_RSP_BUFFER    = (short)512;
	private final static short SIZE_BUFFER_INPUT  = (short)16;
	private final static short SIZE_PACKET_BUFFER = (short)512;
	private final static short SIZE_BUFFER_CHANNEL_DATA = (short)768;
	private final static short SIZE_BUFFER_CHANNEL_CIPHERDATA = (short)1024;
	private final static short SIZE_DEVICE_INFO = (short)106;
	
	private byte[] largeTmpBuf;	// Temporary buffer
	private byte[] smallTmpBuf;	// Temporary buffer
	private byte[] tmpRAM;		// Temporary RAM buffer
	private byte[] inputBuf;	// Use to store input data from user
	private byte[] tempBuf;     // Temporary buffer
	private byte[] BIP_channel_data;		// tag channel_data
	private byte[] BIP_channel_cipherData;	// tag channel_cipherData
	private short[] mnemonicCode;

	
	private HMACKey   key;
	private Signature prf;
	private ECPublicKey publicKey;
	private ECPrivateKey privateKey;
	private Signature signature;
	private RandomData random;

	
	private HDUser R;
	private HDUser C;

	private byte preSignStatus;
	private byte hashNum;
	private byte signVerifyStatus;

	private ToolkitRegistry reg;

	// process command
	private byte  cla;
	private byte  ins;
	private byte  P1;
	private byte  P2;
	private short Lc;
	private short sw;

	private boolean rspReady = false;

	private FileView uiccFileView;	
	
	private Slimduet(byte[] bArray, short bOffset, byte bLength) {

		// Temporary buffer
		tmpRAM = JCSystem.makeTransientByteArray(SIZE_BUFFER_RAM, JCSystem.CLEAR_ON_RESET);
		tempBuf = JCSystem.makeTransientByteArray((short) 660, JCSystem.CLEAR_ON_RESET);
		largeTmpBuf = JCSystem.makeTransientByteArray(SIZE_BUFFER_LARGE, JCSystem.CLEAR_ON_RESET);
		BIP_channel_data = JCSystem.makeTransientByteArray(SIZE_BUFFER_CHANNEL_DATA, JCSystem.CLEAR_ON_RESET);
		BIP_channel_cipherData = JCSystem.makeTransientByteArray(SIZE_BUFFER_CHANNEL_CIPHERDATA, JCSystem.CLEAR_ON_RESET);
		smallTmpBuf = new byte[SIZE_BUFFER_SMALL];
		inputBuf = new byte[SIZE_BUFFER_INPUT];
		mnemonicCode = new short[MNEMONIC_SIZE];
		devInfo = new byte[SIZE_DEVICE_INFO];
		
		SECP256k1.init();
		random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
		key = (HMACKey) KeyBuilder.buildKey(KeyBuilder.TYPE_HMAC_TRANSIENT_RESET, KeyBuilder.LENGTH_HMAC_SHA_512_BLOCK_128, false);
		prf = Signature.getInstance((byte)Signature.ALG_HMAC_SHA_512, true);
		signature = Signature.getInstance(Signature.ALG_ECDSA_SHA_256, false);

		publicKey = (ECPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC, KeyBuilder.LENGTH_EC_FP_256, false);
		privateKey = (ECPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, KeyBuilder.LENGTH_EC_FP_256, false);
		SECP256k1.setCurveParameters(publicKey);
		SECP256k1.setCurveParameters(privateKey);

		pin = new OwnerPIN((byte)PIN_ATTEMPT_NUM, (byte)MAX_PIN_LEN);
		puk = new OwnerPIN((byte)PUK_ATTEMPT_NUM, (byte)MAX_PUK_LEN);		
		pinTryCount = PIN_ATTEMPT_NUM;
		unlockTryCount = PUK_ATTEMPT_NUM;	
		
		menuLanguage = (byte)0;
		hashNum = (byte)0;
		channel_data_length = (short)0;
		ipAndPortRecNum = (short)0xFF;
		channel = (byte)0xFF;
		waitDataDownloadTimes = (byte)0;
		R = null;
		C = null;

		// Register the new applet instance to the JCRE
		if (bArray[bOffset] == (byte)0)
			register();
		else
			register(bArray, (short)(bOffset+1), bArray[bOffset]);	// Register this applet

		// get reference
		reg = ToolkitRegistrySystem.getEntry();
		uiccFileView = UICCSystem.getTheUICCView(JCSystem.CLEAR_ON_RESET);

		// Register event
        reg.setEvent(EVENT_FIRST_COMMAND_AFTER_ATR);
		reg.setEvent(EVENT_EVENT_DOWNLOAD_DATA_AVAILABLE);
		reg.setEvent(EVENT_EVENT_DOWNLOAD_LOCATION_STATUS);
		reg.setEvent(EVENT_PROFILE_DOWNLOAD);
		reg.requestPollInterval((short)30);		// Receive Status command every 30 seconds
	}

	public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
		// Create Java SIM toolkit applet
		Slimduet applet = new Slimduet(bArray, bOffset, bLength);
	}

	/* process applet command
	 * @param apdu
	 */
	public void process(APDU apdu) throws ISOException {
		// Pass selecting AID APDU
		if (selectingApplet() == true)
			return;

		byte[] apduBuf = apdu.getBuffer();
		SecureChannel SC  = GPSystem.getSecureChannel();
		
		switch((short)(Util.getShort(apduBuf, (short)0)&(short)0xFCFF)) {
			case (short)0x8050:		// INITIALIZE UPDATE
			case (short)0x8482:		// EXTERNAL AUTHENTICATION
				Lc = SC.processSecurity(apdu);
				
				if (Lc >= (short)0)
					apdu.setOutgoingAndSend((short)5, Lc);
				
				return;
			case (short)0x80E2:		// STORE DATA
			case (short)0x84E2:
				if (SC.getSecurityLevel() == (short)0)
					ISOException.throwIt((short)0x6985);
				
				Lc = SC.unwrap(apduBuf, (short)0, (short)(5 + apdu.setIncomingAndReceive()));
				Lc -= (short)5;

				sw = processStoredData(apduBuf, (short)0, Lc);
				
				ISOException.throwIt(sw);
				return;
		}
	}

	public boolean select(boolean appInstAlreadyActive) {
		return true;
	}

	public void	deselect(boolean appInstStillActive) {

	}

	public void uninstall() {
		SECP256k1.cleanAllField();
		// Release unused previous objects
		JCSystem.requestObjectDeletion();
	}
	
	/** Toolkit **/
	public void processToolkit(short event) throws ToolkitException {
		switch(event) {
			case EVENT_FIRST_COMMAND_AFTER_ATR:
				init();
				break;
				
			case EVENT_PROFILE_DOWNLOAD:
				bipPauseCounter += (byte)2;
				break;

			case EVENT_EVENT_DOWNLOAD_LOCATION_STATUS:
				checkCurrentService();
				break;
				
			case EVENT_EVENT_DOWNLOAD_DATA_AVAILABLE:
				if (bipPauseCounter != (byte)0) {
					return;
				}

				if (channel != (byte)0xFF) {	// Open channel success
					byte status = (byte)0xFF;
					short apduStatus = (short)0;

					if (BIP_receiveData() == (byte)0) {	// Receive data success
						status = BIP_processData(BIP_channel_data, (short)0);
						/* status:
						 * 0 : close channel
						 * 1 : long polling & don't close channel
						 * 2 : long pooling & close channel
						 * 3 : long polling & pross data & close channel
						 * 4 : server request key change
						*/
						switch(status) {
							case (byte)0:
								BIP_closeChannel();
								break;
							case (byte)1:	
								break;
							case (byte)2:	
								BIP_closeChannel();
								BIP_openChannel();
								if (channel != (byte)0xFF) {	// Open channel success
									BIP_createData((byte)'l');
									BIP_sendData();
									isProcessingBIP = true;
								}
								break;
							case (byte)3:	
								BIP_closeChannel();

								// proess APDU command
								apduStatus =  processCMD(BIP_channel_data, (short)((short)smallTmpBuf[0] & 0x00FF));

								if (!rspReady)
									Lc = (short)0;

								// add apdu status word after the response
								Util.setShort(BIP_channel_data, Lc, apduStatus);
								Lc += (short)2;
								
								BIP_createData((byte)'r');
								connectiveStatus = (byte)'r';
								BIP_openChannel();
								if (channel != (byte)0xFF) {	// Open channel success
									BIP_sendData();
									isProcessingBIP = true;
								}
								isBipRspReady = false;
								break;
							case (byte)4:	
								BIP_closeChannel();
								BIP_openChannel();
								if (channel != (byte)0xFF) {	// Open channel success
									BIP_createData((byte)'k');
									BIP_sendData();
									isProcessingBIP = true;
								}
								break;
							default:
								break;
						}
					}
					else {// Receive data fail
						BIP_closeChannel();
					}

					waitDataDownloadTimes = (byte)0;
				}
				break;
			case EVENT_STATUS_COMMAND:
				if (isProcessingBIP || isBipRspReady || bipPauseCounter != (byte)0) {
					//Close channel after waiting for two status
					if (++waitDataDownloadTimes >= (byte)2) {
						BIP_closeChannel();
						waitDataDownloadTimes = (byte)0;
						isBipRspReady = false;
					}
					if (bipPauseCounter > (byte)0)
						bipPauseCounter--;
				}
				else {
					if (localStatus != (byte)2) {
						BIP_openChannel();
						if (channel != (byte)0xFF) {	// Open channel success
							if(connectiveStatus != (byte)'r') BIP_createData(connectiveStatus);
							BIP_sendData();
							isProcessingBIP = true;
						}
					}
				}
				break;
		}
	}

	private void init() {
		// Temporary Buffer
		Util.arrayFillNonAtomic(largeTmpBuf, (short)0, SIZE_BUFFER_LARGE, (byte)0xFF);
		Util.arrayFillNonAtomic(smallTmpBuf, (short)0, SIZE_BUFFER_SMALL, (byte)0xFF);
		Util.arrayFillNonAtomic(tmpRAM, (short)0, SIZE_BUFFER_RAM, (byte)0xFF);
		Util.arrayFillNonAtomic(inputBuf, (short)0, SIZE_BUFFER_INPUT, (byte)0xFF);


		// BIP
		channel = (byte)0xFF;
		isProcessingBIP = false;
		isBipRspReady = false;
		connectiveStatus = (byte)'k';
		bipPauseCounter = (byte)0;
		isBETag = true;
		rspReady = false;
		getDevInfo();
	}

	private short processStoredData(byte[] apduBuf, short ofs, short apduLen) {
		
		byte pinLength = apduBuf[(short)(ofs + ISO7816.OFFSET_CDATA)];
		byte pukLength = apduBuf[(short)(ofs + ISO7816.OFFSET_CDATA + pinLength)];
		
		pin.update(apduBuf, (short)(ofs + ISO7816.OFFSET_CDATA + 1), pinLength);
		puk.update(apduBuf, (short)(ofs + ISO7816.OFFSET_CDATA + pinLength + 1), pukLength);
		
		return SW_STATUS_NO_ERROR;
	}
	
	private short processCMD(byte[] apduBuf, short offset) {
		cla = apduBuf[(short)(offset+ISO7816.OFFSET_CLA)];
		ins = apduBuf[(short)(offset+ISO7816.OFFSET_INS)];
		P1  = apduBuf[(short)(offset+ISO7816.OFFSET_P1)];
		P2  = apduBuf[(short)(offset+ISO7816.OFFSET_P2)];
		Lc  = (short)(apduBuf[(short)(offset+ISO7816.OFFSET_LC)] & 0xFF);
		sw = ISO7816.SW_NO_ERROR;
		rspReady = false;
		boolean isWalletCmd = true;

		switch(ins) {
			case INS_QUERY_CARD_INFOR:// E0
				if ((cla & (byte)0xFC) != (byte)0x80)
					return ISO7816.SW_CLA_NOT_SUPPORTED;
				
				sw = queryWalletInfo(apduBuf, offset, Lc);
				
				break;
			case INS_CREATE_WALLET_USER:  // E1
				if ((cla & (byte)0xFC) != (byte)0x80)
					return ISO7816.SW_CLA_NOT_SUPPORTED;
				
				sw = manageWalletsUser(apduBuf, offset, Lc);
				
				break;
			case INS_GET_CHILD_PUBLIC_KEY: //E3
				if ((cla & (byte)0xFC) != (byte)0x80)
					return ISO7816.SW_CLA_NOT_SUPPORTED;
				
				sw = getChildPublicKey(apduBuf, offset, Lc);
							
				break;
			case INS_SIGN_TRANSFER: // E4
				if ((cla & (byte)0xFC) != (byte)0x80)
					return ISO7816.SW_CLA_NOT_SUPPORTED;

				sw = signData(apduBuf, offset, Lc);
			
				break;
			case INS_SYNCH_WALLET_INFOR:   //E5
				if ((cla & (byte)0xFC) != (byte)0x80)
					return ISO7816.SW_CLA_NOT_SUPPORTED;
				
				sw =  synchWalletInfo(apduBuf, offset, Lc);
				
				break;
			default:
				isWalletCmd = false;
				break;
		}
		
		if (isWalletCmd) {
			if((sw & (short) 0x6F00) == (short) 0x6100) {
				Lc = (short)(sw & (short)0xFF);
				if (Lc == (short)0)
					Lc = (short) 0x100;
				sw = SW_STATUS_NO_ERROR;
				rspReady = true;
			}
			return sw;
		}
		

		switch(ins) {
			case (byte)0x10:	// GetDevInfo
				// check P1 & P2
				if (P1!=(byte)0 || P2!=(byte)0)
					return ISO7816.SW_INCORRECT_P1P2;

				if (Lc == (short)0) {	// Get devInfo length
					return (short)(ISO7816.SW_CORRECT_LENGTH_00 | SIZE_DEVICE_INFO);
				}
				else {
					// check length
	
					if (Lc != SIZE_DEVICE_INFO)
						return (short)(ISO7816.SW_CORRECT_LENGTH_00 | SIZE_DEVICE_INFO);
					

					Util.arrayFillNonAtomic(apduBuf, (short)0, Lc, (byte)0);		// Clean output buffer
					Util.arrayCopyNonAtomic(devInfo, (short)0, apduBuf, (short)0, SIZE_DEVICE_INFO);
					rspReady = true;
				}

				return sw;
			default:
				sw = ISO7816.SW_COMMAND_NOT_ALLOWED;
				break;
		}


		return sw;
	}



	private void sendDisplayText(byte qualifier, byte dcs, byte[] buffer, short offset, short length) throws ToolkitException{
		try{
			ProactiveHandler prohandler = ProactiveHandlerSystem.getTheHandler();
			prohandler.initDisplayText(qualifier, dcs, buffer, offset, length);
			prohandler.send();
		}catch(ToolkitException e){
			if(e.getReason() == ToolkitException.HANDLER_NOT_AVAILABLE)
				ToolkitException.throwIt(ToolkitException.HANDLER_NOT_AVAILABLE);
		}
	}
	
	public short getInput(byte qualifier, byte dcs ,byte[] displayMsg, short displayMsgLen, short inputMinLen, short inputMaxLen, byte[] textBuf, short textBufOfs) throws ToolkitException{
		try{
			ProactiveHandler prohandler = ProactiveHandlerSystem.getTheHandler();
			ProactiveResponseHandler proRspHandler = ProactiveResponseHandlerSystem.getTheHandler();
			byte inputRes = (byte)0;
			
			if(dcs != (byte)0)
				prohandler.initGetInput(qualifier, dcs, displayMsg, (short)0, displayMsgLen, inputMinLen, inputMaxLen);
			else
				prohandler.initGetInput(qualifier, displayMsg[0], displayMsg, (short)1, (short)(displayMsgLen-1), inputMinLen, inputMaxLen);

			prohandler.send();
			inputMinLen = proRspHandler.copyTextString(textBuf, textBufOfs);
			inputMinLen--;	// Subtract string ending byte '/0'
		}catch(ToolkitException e){
			switch(e.getReason()){
				case ToolkitException.HANDLER_NOT_AVAILABLE:
					ToolkitException.throwIt(ToolkitException.HANDLER_NOT_AVAILABLE);
					break;
				case ToolkitException.UNAVAILABLE_ELEMENT:
					ToolkitException.throwIt(ToolkitException.UNAVAILABLE_ELEMENT);
					break;
			}
		}
		return inputMinLen;	
	}

	private short getWording(byte[] itemData, short itemOfs, short itemNum, short DF, short EF) {
		if(uiccFileView == null)
			uiccFileView = UICCSystem.getTheUICCView(JCSystem.CLEAR_ON_RESET);

		short cnt = (short)0;
		short tmpOfs = (short)0;
		short tmpLength = (short)0;

		uiccFileView.select((short)0x3F00);
		uiccFileView.select(DF);
		uiccFileView.select(EF);

		for (cnt=(short)0; cnt<itemNum; cnt++) {
			uiccFileView.readBinary(tmpOfs, itemData, itemOfs, (short)1);
			tmpLength = (short)(itemData[itemOfs] & 0xFF);
			tmpOfs += (short)(tmpLength + 1);
		}

		uiccFileView.readBinary(tmpOfs, itemData, itemOfs, (short)1);
		tmpLength = (short)(itemData[itemOfs] & 0xFF);
		uiccFileView.readBinary((short)(tmpOfs+1), itemData, itemOfs, (short)tmpLength);

		return tmpLength;
	}
	
	private void getLinearFixedFileItem(short recNum, byte[] bArray, short DF, short EF, short length) {
		if(uiccFileView == null)
			uiccFileView = UICCSystem.getTheUICCView(JCSystem.CLEAR_ON_RESET);

		uiccFileView.select(DF);
		uiccFileView.select(EF);
		uiccFileView.readRecord(recNum, (byte)0x04, (short)0, bArray, (short)0, length);
	}

	/** Device 
		DEVICE_INFO (106) {
			ICCID[33]		// ICCID
			VER[65]		// applet version
			CARD_TYPE[4]	// 00 00 06 00
			PIN_STATUS[1]	// PIN status (0: unmodified, 1: modified)
			RESERVE[3]		// 00 00 00
		}
	 */
	private void getDevInfo() {
		Util.arrayFillNonAtomic(devInfo, (short)0, SIZE_DEVICE_INFO, (byte)0x00);

		if (uiccFileView == null)
			uiccFileView = UICCSystem.getTheUICCView(JCSystem.CLEAR_ON_RESET);

		// bip id
		short length = getCardID(tmpRAM);
		if (tmpRAM[0] != (byte)0) {
			Util.arrayCopyNonAtomic(tmpRAM, (short)0, devInfo, (short)(OFFSET_ICCID+1), length);
			devInfo[OFFSET_ICCID] = (byte)length;
		}

		// version
		Util.setShort(tmpRAM, (short)0, appletVersion);
		simomeTool.convertHexToAscii(tmpRAM, (short)0, devInfo, (short)(OFFSET_VER+1), (short)2);
		devInfo[OFFSET_VER] = (byte)4;
		if (testVersion != (byte)0xFF) {
			devInfo[(short)(OFFSET_VER+5)] = (byte)' ';
			devInfo[(short)(OFFSET_VER+6)] = (byte)'T';
			devInfo[(short)(OFFSET_VER+7)] = (byte)'e';
			devInfo[(short)(OFFSET_VER+8)] = (byte)'s';
			devInfo[(short)(OFFSET_VER+9)] = (byte)'t';
			devInfo[(short)(OFFSET_VER+10)] = testVersion;
			devInfo[OFFSET_VER] = (byte)10;
		}

		// card type
		Util.setShort(devInfo, (short)(OFFSET_CARD_TYPE+2), (short)0x0987);
		// reserve
	}

	private short getCardID(byte[] bArray) {
		short i, j;
		short length = (short)0;
		if(uiccFileView == null)
			uiccFileView = UICCSystem.getTheUICCView(JCSystem.CLEAR_ON_RESET);

		try {
			uiccFileView.select((short)0x3F00);
			uiccFileView.select(FID_DF_BIP);
			uiccFileView.select(FID_EF_CARD_ID);
			uiccFileView.readBinary((short)0, bArray, (short)0, (short)1);
			length = (short)(bArray[0] & 0xFF);
			uiccFileView.readBinary((short)1, bArray, (short)(length * 2), length);
			length *= (short)2;
			for (i=length, j=(short)0; j<length; i++, j+=2) {
				bArray[j] = (byte)((bArray[i] & 0x0F) + 0x30);
				bArray[(short)(j+1)] = (byte)((byte)((byte)(bArray[i] >> 4) & 0x0F) + 0x30);
			}
		}
		catch (UICCException u) {
			Util.arrayFillNonAtomic(bArray, (short)0, (short)16, (byte)0);
		}

		return length;
	}

	private void getTerminalImei(byte[] outputBuff, short outputOffset) {
		ProactiveHandler prohandler = ProactiveHandlerSystem.getTheHandler();
		byte generalRes = (byte)0xFF;
		prohandler.init(PRO_CMD_PROVIDE_LOCAL_INFORMATION, (byte)0x01, DEV_ID_TERMINAL);
		prohandler.send();

		ProactiveResponseHandler proRspHandler = ProactiveResponseHandlerSystem.getTheHandler();
		generalRes = proRspHandler.getGeneralResult();
		if (generalRes == (byte)0x00) {  // Success
			proRspHandler.findAndCopyValue(TAG_IMEI, outputBuff, outputOffset);
		}
	}

	private void sendRefresh() {
		isNoService = false;
		ProactiveHandler prohandler = ProactiveHandlerSystem.getTheHandler();
		prohandler.init(PRO_CMD_REFRESH, (byte)0x04, DEV_ID_TERMINAL);
		prohandler.send();
	}

	private void checkCurrentService() {
		EnvelopeHandler envhandler = EnvelopeHandlerSystem.getTheHandler();
		envhandler.findAndCopyValue(TAG_LOCATION_STATUS, smallTmpBuf, (short)5);
		localStatus =  smallTmpBuf[5];
		if (localStatus == (byte)2)
			isNoService = true;
	}

	private void BIP_openChannel() {
		// If channel does not close yet, then close it before open.
		if (channel != (byte)0xFF)
			BIP_closeChannel();

		if (bipPauseCounter != (byte)0)
			return;

		short port;
		short i = (short)1;
		short current_rec = (ipAndPortRecNum <= (short)3 && ipAndPortRecNum > (short)0)? ipAndPortRecNum : (short)1;
		byte generalRes = (byte)0xFF;
		ProactiveHandler prohandler = ProactiveHandlerSystem.getTheHandler();

		do {
			Util.arrayFillNonAtomic(tmpRAM, (short)4, (short)4, (byte)0xFF); //indiate emptyAddress
			tmpRAM[8] = (byte)0xBE;
			tmpRAM[9] = (byte)0x00;
			getLinearFixedFileItem(current_rec, tmpRAM, FID_DF_BIP, FID_EF_BIP_Port, (short)2);
			port = Util.getShort(tmpRAM, (short)0);
			getLinearFixedFileItem(current_rec, tmpRAM, FID_DF_BIP, FID_EF_BIP_IP, (short)4);

			if (Util.arrayCompare(tmpRAM, (short)0, tmpRAM, (short)4, (short)4) != 0) {
				prohandler.init(PRO_CMD_OPEN_CHANNEL, (byte)0x03, DEV_ID_TERMINAL);
				prohandler.appendTLV(BEARER_DESCRIPTION_TAG, (byte)0x03);	// Default bearer
				prohandler.appendTLV(BUFFER_SIZE_TAG, (short)0x05DC);
				if (isBETag) 	// iPhone12 can't use tag "BE"
					prohandler.appendArray(tmpRAM, (short)8, (short)2);			// Add "BE 00"
				prohandler.appendTLV(UICC_TERMINAL_TRANSPORT_LEVEL_TAG, (byte)0x02, port);	// TCP, port number
				prohandler.appendTLV(OTHER_DATA_DESTINATION_ADDRESS_TAG, (byte)0x21, tmpRAM, (short)0, (short)4);
				prohandler.send();

				ProactiveResponseHandler proRspHandler = ProactiveResponseHandlerSystem.getTheHandler();
				generalRes = proRspHandler.getGeneralResult();


				if(generalRes == RES_ERROR_CMD_BEYOND_TERMINAL_CAPAB){
					isBETag = !isBETag;

					prohandler.init(PRO_CMD_OPEN_CHANNEL, (byte)0x03, DEV_ID_TERMINAL);
					prohandler.appendTLV(BEARER_DESCRIPTION_TAG, (byte)0x03);	// Default bearer
					prohandler.appendTLV(BUFFER_SIZE_TAG, (short)0x05DC);
					if(isBETag)
						prohandler.appendArray(tmpRAM, (short)8, (short)2);			// Add "BE 00"
					prohandler.appendTLV(UICC_TERMINAL_TRANSPORT_LEVEL_TAG, (byte)0x02, port);	// TCP, port number
					prohandler.appendTLV(OTHER_DATA_DESTINATION_ADDRESS_TAG, (byte)0x21, tmpRAM, (short)0, (short)4);
					prohandler.send();

					generalRes = proRspHandler.getGeneralResult();
				}

				if(generalRes < (byte)0x0A) {  // Success
					channel = proRspHandler.getChannelIdentifier();
					ipAndPortRecNum = current_rec;
					return;
				}
				else if (generalRes == RES_ERROR_BEARER_INDEPENDENT_PROTOCOL_ERROR && localStatus != (byte)2 && isNoService) {
					sendRefresh();
				}
			}
			current_rec = (current_rec == (short)3) ? (short)1 : current_rec++;
			i++;
		} while(i <= (short)3);
	}

	private void BIP_closeChannel() {
		if (channel == (byte)0xFF)
			return;

		ProactiveHandler prohandler = ProactiveHandlerSystem.getTheHandler();

		prohandler.initCloseChannel((byte)(0x20+channel));
		prohandler.send();
	}

	private void BIP_sendData() {
		short ofs = (short)0;
		short length = channel_data_length;
		tmpRAM[0] = CHANNEL_DATA_TAG;
		ProactiveHandler prohandler = ProactiveHandlerSystem.getTheHandler();

		while (length >= (short)0x00F0) {
			prohandler.init(PRO_CMD_SEND_DATA, (byte)0x01, (byte)(0x20+channel));
			Util.setShort(tmpRAM, (short)1, (short)0x81F0);
			Util.arrayCopyNonAtomic(BIP_channel_cipherData, ofs, tmpRAM, (short)3, (short)0x00F0);
			prohandler.appendArray(tmpRAM, (short)0, (short)0x00F3);

			prohandler.send();

			ofs += (short)0x00F0;
			length -= (short)0x00F0;
		}

		if (length > (short)0x007F) {
			prohandler.init(PRO_CMD_SEND_DATA, (byte)0x01, (byte)(0x20+channel));
			tmpRAM[1] = (byte)0x81;
			tmpRAM[2] = (byte)length;
			Util.arrayCopyNonAtomic(BIP_channel_cipherData, ofs, tmpRAM, (short)3, length);
			prohandler.appendArray(tmpRAM, (short)0, (short)(length + 3));

			prohandler.send();
		}
		else if (length > (short)0) {
			prohandler.init(PRO_CMD_SEND_DATA, (byte)0x01, (byte)(0x20+channel));
			tmpRAM[1] = (byte)length;
			Util.arrayCopyNonAtomic(BIP_channel_cipherData, ofs, tmpRAM, (short)2, length);
			prohandler.appendArray(tmpRAM, (short)0, (short)(length + 2));

			prohandler.send();

		}

		if(connectiveStatus == (byte)'r') connectiveStatus = (byte)'l';

	}

	private byte BIP_receiveData() {
		if(bipPauseCounter != (byte)0)
			return (byte)1;

		short len = (short)0;
		short cnt = (short)0;
		short totalLength = (short)0;
		byte generalRes = (byte)0xFF;
		byte flag = (short)0;
		
		ProactiveHandler prohandler = ProactiveHandlerSystem.getTheHandler();
		ProactiveResponseHandler proRspHandler;

		EnvelopeHandler envhandler = EnvelopeHandlerSystem.getTheHandler();
		envhandler.findAndCopyValue(TAG_CHANNEL_DATA_LENGTH, smallTmpBuf, (short)0);

		while (smallTmpBuf[0] != (byte)0x00) {	// Still have data
			prohandler.init(PRO_CMD_RECEIVE_DATA, (byte)0, (byte)(0x20+channel));
			prohandler.appendTLV(CHANNEL_DATA_LENGTH_TAG, smallTmpBuf[0]);
			prohandler.send();

			proRspHandler = ProactiveResponseHandlerSystem.getTheHandler();
			generalRes = proRspHandler.getGeneralResult();

			if (generalRes < (byte)0x20) {
				if (flag == (byte)0) {
					proRspHandler.findAndCopyValue(TAG_CHANNEL_DATA_LENGTH, smallTmpBuf, (short)0);
					len = proRspHandler.findAndCopyValue(TAG_CHANNEL_DATA, largeTmpBuf, (short)0);
					for (cnt=(short)0; cnt<len; cnt++) {
						if (largeTmpBuf[cnt]==(byte)0x0D && largeTmpBuf[(short)(cnt+1)]==(byte)0x0A && largeTmpBuf[(short)(cnt+2)]==(byte)0x0D && largeTmpBuf[(short)(cnt+3)]==(byte)0x0A) {
							// Skip 0D 0A 0D 0A
							if (cnt != (short)0) {
								//skip second 0D 0A 0D 0A
								cnt += (short)8;
								len -= cnt;
							}
							else {
								cnt += (short)4;
								len -= cnt;
							}

							Util.arrayCopyNonAtomic(largeTmpBuf, (short)(cnt+4), BIP_channel_data, (short)0, len);
							totalLength = len;
							flag = (byte)1;
							break;
						}
					}
				}
				else {
					len = proRspHandler.copyChannelData(BIP_channel_data, (short)totalLength, (short)((short)smallTmpBuf[0] & 0x00FF));
					proRspHandler.findAndCopyValue(TAG_CHANNEL_DATA_LENGTH, smallTmpBuf, (short)0);
				}
			}
			else 
				return 1;
		}

		return (byte)0;
	}

	private void BIP_createData(byte connective) {
		short tagLen = (short)0;
		short bufOfs = (short)0;
		short responseDataLen = (short)0;

		if (connective == (byte)'r') {
			responseDataLen = Lc;
			responseDataLen = simomeTool.convertHexToBase64Url(BIP_channel_data,(short)0, responseDataLen, BIP_channel_cipherData, (short)0);
		}

		// connectiveStatus
		BIP_channel_data[bufOfs++] = (byte)connective;
		tagLen++;
		BIP_channel_data[bufOfs++] = (byte)'/';
		tagLen++;

		// token
		random.generateData(tmpRAM, (short)0, (short)4);
		preToken = Util.getShort(tmpRAM, (short)0);
		simomeTool.convertHexToAscii(tmpRAM, (short)0, BIP_channel_data, bufOfs, (short)2);
		bufOfs += (short)4;
		tagLen += (short)4;
		BIP_channel_data[bufOfs++] = (byte)'/';
		tagLen++;

		switch(connective) {
			case's':
			case'l':
				// pogarm
				BIP_channel_data[bufOfs++] = (byte)0x01;
				tagLen++;

				// You can do encryption here
				Util.arrayCopyNonAtomic(BIP_channel_data, (short)0, BIP_channel_cipherData, (short)0, tagLen);

				// conver to Base64Url
				tagLen = simomeTool.convertHexToBase64Url(BIP_channel_cipherData,(short)0, tagLen, BIP_channel_data, (short)0);
				responseDataLen = tagLen;

				break;
			case'r':
				// pogarm
				BIP_channel_data[bufOfs++] = (byte)0x01;
				tagLen++;
				BIP_channel_data[bufOfs++] = (byte)'/';
				tagLen++;

				// serial number
				Util.setShort(tmpRAM, (short)0, currentSerialNum);
				simomeTool.convertHexToAscii(tmpRAM, (short)0, BIP_channel_data, bufOfs, (short)2);
				bufOfs += (short)4;
				tagLen += (short)4;
				BIP_channel_data[bufOfs++] = (byte)'/';
				tagLen++;

				// response data
				Util.arrayCopyNonAtomic(BIP_channel_cipherData, (short)0, BIP_channel_data, bufOfs, responseDataLen);
				bufOfs += responseDataLen;
				tagLen += responseDataLen;

				// TODO : You can do encryption here
				Util.arrayCopyNonAtomic(BIP_channel_data, (short)0, BIP_channel_cipherData, (short)0, tagLen);


				// conver to Base64Url
				tagLen = simomeTool.convertHexToBase64Url(BIP_channel_cipherData,(short)0, tagLen, BIP_channel_data, (short)0);
				responseDataLen = tagLen;

				break;
			case'k':
				// get seesion key
				responseDataLen = simomeTool.convertHexToBase64Url(MasterSessionKey,(short)0, (short)MasterSessionKey.length, BIP_channel_data, bufOfs);
				bufOfs += responseDataLen;
				tagLen += responseDataLen;
				BIP_channel_data[bufOfs++] = (byte)'/';
				tagLen++;

				// IMEI
				getTerminalImei(tmpRAM,(short)0);
				simomeTool.convertHexToAscii(tmpRAM, (short)0, BIP_channel_data, bufOfs, (short)8);
				bufOfs += (short)16;
				tagLen += (short)16;
				BIP_channel_data[bufOfs++] = (byte)'/';
				tagLen++;
					
				//TODO : You can do encryption here
				Util.arrayCopyNonAtomic(BIP_channel_data, (short)0, BIP_channel_cipherData, (short)0, tagLen);
				
				// conver to Base64Url
				tagLen = simomeTool.convertHexToBase64Url(BIP_channel_cipherData,(short)0, tagLen, BIP_channel_data, (short)0);
				responseDataLen = tagLen;

				break;
		}

		bufOfs = (short)0;
		tagLen = (short)0;
		
		Util.arrayCopyNonAtomic(BIP_HTTP_HEAD, (short)0, BIP_channel_cipherData, bufOfs, (short)BIP_HTTP_HEAD.length);
		bufOfs += BIP_HTTP_HEAD.length;
		tagLen += BIP_HTTP_HEAD.length;

		short length;
		short ofs = (short)0;
		short s;

		Util.arrayCopyNonAtomic(BIP_HTTP_CONTENT1, (short)0, BIP_channel_cipherData, bufOfs, (short)BIP_HTTP_CONTENT1.length);
		bufOfs += BIP_HTTP_CONTENT1.length;
		tagLen += BIP_HTTP_CONTENT1.length;

		s = simomeTool.convertShortToBcd(responseDataLen);
		Util. setShort(tmpRAM, (short)2, s);
		simomeTool.convertHexToAscii(tmpRAM, (short)2, tmpRAM, (short)0, (short)2);
		length = (short)4;
		while (tmpRAM[ofs] == (byte)'0' && length != (short)0) {
			ofs++;
			length--;
		}

		// content-length
		Util.arrayCopyNonAtomic(tmpRAM, ofs, BIP_channel_cipherData, bufOfs, length);
		bufOfs += length;
		tagLen += length;

		// Card id
		Util.arrayCopyNonAtomic(BIP_HTTP_CONTENT, (short)0, BIP_channel_cipherData, bufOfs, (short)BIP_HTTP_CONTENT.length);
		bufOfs += BIP_HTTP_CONTENT.length;
		tagLen += BIP_HTTP_CONTENT.length;
		// Card ID
		length = getCardID(tmpRAM);
		Util.arrayCopyNonAtomic(tmpRAM, (short)0 ,BIP_channel_cipherData, bufOfs, length);
		bufOfs += length;
		tagLen += length;

		if (connective == (byte)'k') {
			BIP_channel_cipherData[bufOfs++] = (byte)',';
			tagLen++;
			BIP_channel_cipherData[bufOfs++] = (byte)'k';
			tagLen++;
		}

		Util.arrayCopyNonAtomic(BIP_HTTP_END, (short)0, BIP_channel_cipherData, bufOfs, (short)BIP_HTTP_END.length);
		bufOfs += BIP_HTTP_END.length;
		tagLen += BIP_HTTP_END.length;

		Util.arrayCopyNonAtomic(BIP_channel_data, (short)0, BIP_channel_cipherData, bufOfs, responseDataLen);
		bufOfs += responseDataLen;
		tagLen += responseDataLen;

		channel_data_length = tagLen;
	}

	/* Process BIP response data
	 *
	 * return status
	 * 0 : close channel
	 * 1 : long polling & don't close channel
	 * 2 : long pooling & close channel
	 * 3 : long polling & pross data & close channel
	 * 4 : server request key change
	 */
	private byte BIP_processData(byte[] bArray, short offset) {
		connectiveStatus = bArray[offset++];
		byte mode = bArray[offset++];
		short token = Util.getShort(bArray, offset);
		offset += (short)2;
		byte OS = bArray[offset++];
		short serialNum;
		byte status = (byte)0;
		short itemLen;
		
		switch(connectiveStatus) {
			case (byte)'s':
				if (mode == (byte)0xFB) {
					status = (byte)0;
				}
				break;
			case (byte)'l':
				if (mode == (byte)0x00)
					status = (byte)1;
				else if (mode == (byte)0xFB)
					status = (byte)2;
				else if (mode == (byte)0xFC) {
					currentSerialNum = Util.getShort(bArray, offset);
					offset += (short)2;
					smallTmpBuf[0] = (byte)offset;
					status = (byte)3;
					isBipRspReady = true;
				}
				break;
			case (byte)'k':
				if(mode == (byte)0xFA) {
					status = (byte)4;
					return status;
				}	
				else if(mode == (byte)0xFB){
					if(bArray[offset] > (byte)0)
						bipPauseCounter = bArray[offset];
				}				
				else if (mode == (byte)0xFC) {
					// keychange_ok
					if (Util.arrayCompare(bArray, offset, checkKeyChange, (short)0, (short)checkKeyChange.length) == (byte)0)
						connectiveStatus = (byte)'s';
				}
				status = (byte)0;
				break;
			default:
				connectiveStatus = (byte)'k';
				Util.arrayFillNonAtomic(bArray, (short)0, (short)bArray.length, (byte)0);
				return status;
		}

		if (preToken != token)
			status = (byte)0;

		return status;
	}

	/**
	 * Derivative Master Key from salt with specific parameters for BIP39. With Iteration Count 2048, Length of MK is 512 bits
	 * @param salt             is byte array contains Salt data from offset of <i>offset_salt</i> and byte length of <i>length_salt</i>
	 * @param offset_salt      is offset of the first byte of Salt data in byte array <i>salt</i>
	 * @param length_salt      length of Salt data, at least 16 bytes, at most 123 bytes.
	 * @param out_mk           is output transient byte array, contains 64 bytes derived Master Key from offset of <i>offset_mk</i>
	 * @param offset_mk        is offset of the first byte of derived Master Key in byte array <i>out_mk</i>
	 * @param Ui			   is transient byte array temporary used during the calculation, should be Transient array to speed up the calculation.
	 * @param offset_Ui		   is offset of the first usable byte in byte array <i>Ui</i>, usable size should be 64 bytes.
	 * @remark    SALT, MK and UI can be the same array, but their data should not be overlayed. MK and UI must be transient array or global array
	 *            such as apdu buffer or returned array of UICCPlatform.getTheVolatileByteArray();
	 * @exception CryptoException - with the following reason code:<ul>
	 *            <li>CryptoException.ILLEGAL_VALUE if any parameter is invalid or out of range.
	 *            <li>CryptoException.UNINITIALIZED_KEY if the Key instance is uninitialized.
	 **/
	private void PBKDF2_BIP39(byte[] salt, short offset_salt, short length_salt, byte[] out_mk, short offset_mk, byte[] Ui, short offset_Ui) throws CryptoException {
		//if (length_salt<(short)16) CryptoException.throwIt(CryptoException.ILLEGAL_VALUE);

		Util.arrayFillNonAtomic(Ui, offset_Ui, (short)3, (byte)0);
		Ui[(short)(offset_Ui+3)] = (byte)1;

		prf.update(salt, (short)offset_salt, length_salt);
		prf.sign(Ui, offset_Ui, (short)4, Ui, offset_Ui);
		Util.arrayCopyNonAtomic(Ui, offset_Ui, out_mk, offset_mk, (short)64);

		for (short c=(short)2047; c>(short)0; c--) {
			// Calculate HMAC
			prf.sign(Ui, offset_Ui, (short)64, Ui, offset_Ui);

			for (short i=(short)63; i>=(short)0; i--) {
				out_mk[(short)(offset_mk + i)] ^= Ui[(short)(offset_Ui + i)];
			}
		}
	}

	private short queryWalletInfo(byte[] apduBuf, short offset, short apduLen) {
		if ((P1 != (byte)0) || (P2 != (byte)0))
			return ISO7816.SW_INCORRECT_P1P2;

		if (P1 == (byte)0) {		// walletIds
			short resLen = HDUser.readAllUser(R, apduBuf, (short)0);
			if (resLen == (short)0)
				return SW_HD_USER_NO_EXIST;

			return (short)(0x6100 | resLen);
		}

		return SW_STATUS_NO_ERROR;
	}

	private short manageWalletsUser(byte[] apduBuf, short offset, short apduLen) {
		Util.arrayCopyNonAtomic(apduBuf, offset, largeTmpBuf, (short)0, (short)(apduLen+5));	// backup apdu command
		
		boolean checkImport;
		short itemLen;
		byte p1 = largeTmpBuf[ISO7816.OFFSET_P1];
		byte p2 = largeTmpBuf[ISO7816.OFFSET_P2];

		// check P1P2 value
		if (p1 > 1)
			return ISO7816.SW_INCORRECT_P1P2;

		if (p1 == 0) {		// Create User
			if (HDUser.readAllUserCount(R) >= HD_USER_NUM_MAX)
				return SW_HD_USER_OVERSIZE;

			if (p2 > 2)
				return ISO7816.SW_INCORRECT_P1P2;	// check P2

			if (p2 == 0) {
				GenerateMnemonic(largeTmpBuf, ISO7816.OFFSET_LC);
				if((sw = verifyPayCode_STK()) == SW_STATUS_NO_ERROR){
					if(!isUnBlockedWithPuk)
						creatPayCode_STK();
					else
						isUnBlockedWithPuk = false;						
					creatWallet_STK(largeTmpBuf, ISO7816.OFFSET_LC);					
				}
				else{
					if(isUnBlockedWithPuk){
						isUnBlockedWithPuk = false;			
						creatWallet_STK(largeTmpBuf, ISO7816.OFFSET_LC);
					}
					else
						return sw;	
				} 						
			}
			else if (p2 == 2) {
				if((sw = verifyPayCode_STK()) == SW_STATUS_NO_ERROR){				
					if(!isUnBlockedWithPuk)
						creatPayCode_STK();
					else
						isUnBlockedWithPuk = false;
					checkImport = importWallet_STK(largeTmpBuf, ISO7816.OFFSET_LC);
					if(!checkImport) return SW_DATA_INVALID;
				}
				else{
					if(isUnBlockedWithPuk){
						isUnBlockedWithPuk = false;			
						checkImport = importWallet_STK(largeTmpBuf, ISO7816.OFFSET_LC);
						if(!checkImport) return SW_DATA_INVALID;
					}
					else
						return sw;	
				}					
			}
			
			short resLen = HDUser.readAllUser(R, tempBuf, (short)0);
			if(resLen >= USER_ID_SIZE){
				short index;
				short i;
				for(index = (short)0; index < (short)resLen; index += USER_ID_SIZE){
					C = HDUser.read(R, tempBuf, index);
					for(i = (short)0; i < MNEMONIC_SIZE; i++){
						if(mnemonicCode[i] != C.getMnemonicCode(i))
							break;
					}
					if(i == MNEMONIC_SIZE){
						if(p2 != 1){
							itemLen = getWording(tempBuf, (short)0, (short)14, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Wallet already exists
							sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);							
						}
						return SW_HD_WALLET_EXIST;
					}
				}
			}
			
			key.setKey(largeTmpBuf, ISO7816.OFFSET_CDATA, largeTmpBuf[ISO7816.OFFSET_LC]);
			prf.init(key, Signature.MODE_SIGN);

			PBKDF2_BIP39(MNEMONIC_WORD, (short)0, (short)MNEMONIC_WORD.length, /* SALT, maximal 123 bytes */
						largeTmpBuf, (short)0,   		/* MK output, 64 bytes */
						largeTmpBuf, (short)64);	/* Temporary Buffer, 64 bytes*/

			// Generate masterKey
			Crypto.hmacSHA512(BITCOIN_SEED, (short)0, (short)BITCOIN_SEED.length, largeTmpBuf, (short)0, (short)64, tempBuf, (short)0);

			C = HDUser.createUser(R);	
			if(R == null)
				R = C;

			C.getMasterPrivate().setS(tempBuf, (short)0, KEY_SECRET_SIZE); //Masterkey 32 byte set
			Util.arrayCopy(tempBuf, KEY_SECRET_SIZE, C.getMasterChainCode(), (short)0, CHAIN_CODE_SIZE); //CHAIN_CODE_SIZE=32
			C.setMnemonicCode(mnemonicCode, (short)0, (byte)(mnemonicCode.length)); // store MnemonicCode
			

			// generate User ID
			tempBuf[0] = (byte)0x80;
			tempBuf[1] = (byte)0x0F;
			tempBuf[2] = (byte)0x42;
			tempBuf[3] = (byte)0x40;
			deriveKey(tempBuf, (short)0, (short)4, tempBuf, (short)4, largeTmpBuf, (short)0);
			Util.arrayCopy(largeTmpBuf, (short)0, C.getUserID(), (short)0, USER_ID_SIZE);
			
			
			if(p2 == (byte)0){
				itemLen = getWording(tempBuf, (short)0, (short)15, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Wallet Created successfully
				sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);	
			}else if(p2 == (byte)2){
				itemLen = getWording(tempBuf, (short)0, (short)16, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Wallet Imported successfully
				sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);					
			}

			return SW_STATUS_NO_ERROR;
		}
		else if (p1 == 1) {	// Delete User
			if (apduLen != USER_ID_SIZE)
				return ISO7816.SW_WRONG_LENGTH;

			if ((C = HDUser.read(R, largeTmpBuf, ISO7816.OFFSET_CDATA)) == null)
				return SW_HD_USER_NO_EXIST;
		
			if((sw = verifyPayCode_STK()) == SW_STATUS_NO_ERROR)
				C = HDUser.deleteUser(R, largeTmpBuf, ISO7816.OFFSET_CDATA, tempBuf, (short)0);
			else 
				return sw;
				
			
			if (tempBuf[0] != HDUser.NO_USER) {
				if (tempBuf[0] == HDUser.ONE_USER)
					R = C;
					C = null;
			}
			else {
				return SW_HD_USER_NO_EXIST;
			}
			
			itemLen = getWording(tempBuf, (short)0, (short)17, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Wallet deleted successfully
			sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
			
			return SW_STATUS_NO_ERROR;
		}

		return SW_STATUS_NO_ERROR;
	}

	private void GenerateMnemonic(byte[] bArray, short offset) {	
		random.generateData(tempBuf, (short)0, (short)16);
		Crypto.getSHA256(tempBuf, (short)0, (short)16, tempBuf, (short)16);
		cut12cells(tempBuf);		

		short cnt;
		short itemLen;
		short ofs = (short)1;

		for (cnt = (short)0; cnt < (short)12; cnt++) {
			itemLen = searchWordList(bArray, (short)(offset+ofs), mnemonicCode[cnt]);
			ofs += itemLen;
			bArray[(short)(offset+ofs)] = (byte)0x20;
			ofs++;
		}

		bArray[offset] = (byte)(ofs-2);
	}

	private void cut12cells(byte[] seed) {
		short temp1;
		short temp2;
		//mnemonic code 1
		temp1 = Util.getShort(seed, (short)0); 
		temp2 = (short)((temp1 >> 5) & 0x7ff);
		Util.setShort(tempBuf, (short)17, temp2);
		//mnemonic code 2
		temp1 = Util.getShort(seed, (short)1); 
		Util.setShort(tempBuf, (short)19, (short)((temp1 >> 2) & 0x7ff));
		//mnemonic code 3
		temp1 = (short)(Util.getShort(seed, (short)2) << 1 & 0x7fe); 
		temp2 = (short)(Util.getShort(seed, (short)3) >> 7 & 0x01);
		temp1 = (short)(temp1 | temp2);
		Util.setShort(tempBuf, (short)21, temp1);
		//mnemonic code 4
		temp1 = Util.getShort(seed, (short)4);
		Util.setShort(tempBuf, (short)23, (short)((temp1 >> 4) & 0x7ff));
		//mnemonic code 5
		temp1 = Util.getShort(seed, (short)5); 
		Util.setShort(tempBuf, (short)25, (short)((temp1 >> 1) & 0x7ff));
		//mnemonic code 6
		temp1 = (short)(Util.getShort(seed, (short)6) << 2 & 0x7fc); //s6
		temp2 = (short)(Util.getShort(seed, (short)7) >> 6 & 0x03);
		temp1 = (short)(temp1 | temp2);
		Util.setShort(tempBuf, (short)27, temp1);
		//mnemonic code 7
		temp1 = Util.getShort(seed, (short)8); 
		Util.setShort(tempBuf, (short)29, (short)((temp1 >> 3) & 0x7ff));
		//mnemonic code 8
		temp1 = Util.getShort(seed, (short)9);
		Util.setShort(tempBuf, (short)31, (short)(temp1 & 0x7ff));
		//mnemonic code 9
		temp1 = Util.getShort(seed, (short)11); 
		Util.setShort(tempBuf, (short)33, (short)((temp1 >> 5) & 0x7ff));
		//mnemonic code 10
		temp1 = Util.getShort(seed, (short)12); 
		Util.setShort(tempBuf, (short)35, (short)((temp1 >> 2) & 0x7ff));
		//mnemonic code 11
		temp1 = (short)(Util.getShort(seed, (short)13) << 1 & 0x7fe); 
		temp2 = (short)(Util.getShort(seed, (short)14) >> 7 & 0x01);
		temp1 = (short)(temp1 | temp2);
		Util.setShort(tempBuf, (short)37, temp1);
		//mnemonic code 12
		temp1 = Util.getShort(seed, (short)15); 
		Util.setShort(tempBuf, (short)39, (short)((temp1 >> 4) & 0x7ff));
		temp2 = (short)0;
		
		for (temp1 = (short)17; temp1 < (short)41; temp1 += (short)2) {
			mnemonicCode[temp2++] = Util.getShort(tempBuf, (short)temp1);
		}
	}
	
	private boolean validateMnemonic(){
		short i,bi;
		byte sha;
		byte lastByte;
		byte mask = (byte)0xF0;
		Util.arrayFillNonAtomic(tempBuf, (short)0, (short)17, (byte)0);
		Util.arrayFillNonAtomic(tmpRAM, (short)0, (short)17, (byte)0);
		for(i = (short)0, bi = (short)0; i < (short)12; i++, bi+=11){
			writeNext11bits(tmpRAM, mnemonicCode[i], bi);
		}
		Crypto.getSHA256(tmpRAM, (short)0, (short)16, tempBuf, (short)0);
		sha = tempBuf[0];
		lastByte = tmpRAM[16];
		return (byte)((byte)(sha ^ lastByte) & mask) == 0;
	}
	
	private void writeNext11bits(byte[] bytes, short value, short offset){
		short skip = (short)(offset / 8);
		short bitSkip = (short)(offset % 8);
		short i;
		byte firstValue;
		byte toWrite;
		byte valueInByte;
		
		//byte 0
		firstValue = bytes[skip];
		toWrite = (byte) (value >> (short)(3 + bitSkip));
		bytes[skip] = (byte) (firstValue | toWrite);
		

		//byte 1
		valueInByte = bytes[(short)(skip + 1)];
		i = (short)(5 - bitSkip);
		toWrite = (byte) (i > 0 ? (value << i) : (value >> -i));
		bytes[(short)(skip + 1)] = (byte) (valueInByte | toWrite);
		

		if (bitSkip >= 6) {//byte 2
			valueInByte = bytes[(short)(skip + 2)];
			toWrite = (byte) (value << 13 - bitSkip);
			bytes[(short)(skip + 2)] = (byte) (valueInByte | toWrite);
		}
	}
	
	private boolean compareWordList(byte[] inputArray , short inputOffset, short inputLength , byte[] dstArray, short wordNum){
		
		short alphabeOfs, nextAlphabeOfs;
		short i;
		for(i = (short)0; i < (short)26; i++){
			if((inputArray[inputOffset] == (byte)(i + 0x61)) && (i != (short)23)){
				break;
			}

		}
		
		if(i < (short)26){
			alphabeOfs = ALPHABE_INDEX[i++];
			nextAlphabeOfs = ALPHABE_INDEX[i];
			
			while(alphabeOfs <= nextAlphabeOfs){
				uiccFileView.select(FID_DF_COLDWALLET);
				uiccFileView.select(FID_EF_MENU_MNEMONICWORD);
				uiccFileView.readBinary(alphabeOfs, dstArray, (short)0, (short)9);
				if(dstArray[0] == (byte)inputLength){
					if(Util.arrayCompare(dstArray, (short)1, inputArray, inputOffset, inputLength) == (byte)0){
						mnemonicCode[wordNum] = (short)(alphabeOfs / 9);
						return true;
					}
				}
				alphabeOfs += (short)9;
			}
		}
		

		return false;
		
	}

	private short searchWordList(byte[] bArray,short offset ,short num) {
		short ofs = (short)((short)(num * 9) & 0x7FFF);


		if (uiccFileView == null)
			uiccFileView = UICCSystem.getTheUICCView(JCSystem.CLEAR_ON_RESET);

		uiccFileView.select(FID_DF_COLDWALLET);
		uiccFileView.select(FID_EF_MENU_MNEMONICWORD);
		uiccFileView.readBinary(ofs, tempBuf, (short)0, (short)9);
		Util.arrayCopyNonAtomic(tempBuf, (short)1, bArray, offset, (short)tempBuf[0]);

		return (short)(tempBuf[0] & 0x00FF);
	}

	/**
	 * Processes the DERIVE KEY command.  The master key must be already loaded and have a chain code.
	 * this method is quite straightforward, since it takes a sequence of 32-bit big-endian integers and perform key
	 * derivations, updating the current key path accordingly.
	 * In all cases transactions are used to make sure that the current key is always complete (private, chain and public
	 * components are coherent) and the key path matches the actual status of the card. This makes recovery from a sudden
	 * power loss easy.
	 *
	 * When the reset flag is set and the data is empty, the assisted key derivation flag is ignored, since in this case
	 * no derivation is done and the master key becomes the current key.
	 *
	 * @param apdu the JCRE-owned APDU object.
	 */
	private short getChildPublicKey(byte[] apduBuf, short offset, short apduLen) {
		// check P1P2 value
		if ((apduBuf[(short)(offset+ISO7816.OFFSET_P1)] != 0) || (apduBuf[(short)(offset+ISO7816.OFFSET_P2)] != 0))
			return ISO7816.SW_INCORRECT_P1P2;

		if (apduLen != USER_ID_SIZE+COMPLETE_USER_PATH_SIZE)
			return ISO7816.SW_WRONG_LENGTH;

		if ((C = HDUser.read(R, apduBuf, (short)(offset+ISO7816.OFFSET_CDATA))) == null)
			return SW_HD_USER_NO_EXIST;

		Util.arrayCopyNonAtomic(apduBuf, (short)(offset+ISO7816.OFFSET_CDATA+USER_ID_SIZE), tempBuf, (short)0, COMPLETE_USER_PATH_SIZE);

		short offEnd = (short)(offset + ISO7816.OFFSET_CDATA + apduLen);

		deriveKey(apduBuf, (short)(offset+ISO7816.OFFSET_CDATA+USER_ID_SIZE), offEnd, apduBuf, (short)(offset+offEnd), tempBuf, COMPLETE_USER_PATH_SIZE);

		Util.arrayCopyNonAtomic(tempBuf, COMPLETE_USER_PATH_SIZE, apduBuf, (short)0, (short)(KEY_SECRET_SIZE*2));

		return (short)((short)0x6100 | (short)(KEY_SECRET_SIZE*2));			
		

	}

	// Child key derivation (CKD)
	private void deriveKey(byte[] src, short srcOff, short len, byte[] key, short keyOff, byte[] out, short outOff) {
		short pubLen = (short)0;
		short chnOff = (short)(keyOff+KEY_SECRET_SIZE);

		// Recover key
		C.getMasterPrivate().getS(key, keyOff);
		Util.arrayCopyNonAtomic(C.getMasterChainCode(), (short)0, key, chnOff, CHAIN_CODE_SIZE);

		for (short i = srcOff; i < len; i += 4) {
			
			if ((src[i] & (byte)0x80) != (byte)0x80) {	// normal child
				privateKey.setS(key, keyOff, KEY_SECRET_SIZE);
				pubLen = SECP256k1.derivePublicKey(privateKey, out, (short)(outOff+1));

				pubLen = (short) (outOff + pubLen);
				out[outOff] = ((out[pubLen] & 1) != 0 ? (byte)0x03 : (byte)0x02);
			}
			else {	// Check whether i = 231 (whether the child is a hardened key)
				out[outOff] = (byte)0;
				Util.arrayCopyNonAtomic(key, keyOff, out, (short)(outOff + 1), KEY_SECRET_SIZE);
			}

			if (!Crypto.bip32CKDPriv(src, i, key, keyOff, out, outOff, key, chnOff)) {
				ISOException.throwIt(ISO7816.SW_DATA_INVALID);
				return;
			}
			
		}

		privateKey.setS(key, keyOff, KEY_SECRET_SIZE);
		SECP256k1.derivePublicKey(privateKey, out, (short)outOff);
		Util.arrayFillNonAtomic(key, keyOff, KEY_SECRET_SIZE, (byte)0);
	}

	private short signData(byte[] apduBuf, short offset, short apduLen) throws ToolkitException{
		if(R == null) return SW_HD_USER_NO_EXIST;
		
		if (P1 == (byte)0)
			preSignStatus = P1;
		
		if (P1 > (byte)2)
			return ISO7816.SW_INCORRECT_P1P2;

		if (((P1 < preSignStatus) && (P1 != (byte)0)) || (P1 > (byte)(preSignStatus+1)))
			return ISO7816.SW_DATA_INVALID;
		
		short begin, end;
		short hashNumOff = (short)(USER_ID_SIZE + COMPLETE_USER_PATH_SIZE);
		
		switch(P1){
			case 0:
				Util.arrayCopyNonAtomic(apduBuf, (short)(offset+ISO7816.OFFSET_CDATA), largeTmpBuf, (short)0, apduLen); //backup data
				signVerifyStatus = (byte)1; 
				try{
					if((sw = verifyPayCode_STK()) != SW_STATUS_NO_ERROR){
						signVerifyStatus = (byte)2;
						return sw;
					}
				}catch(ToolkitException e){
					switch(e.getReason()){
						case ToolkitException.HANDLER_NOT_AVAILABLE:
							ToolkitException.throwIt(ToolkitException.HANDLER_NOT_AVAILABLE);
							break;
						case ToolkitException.UNAVAILABLE_ELEMENT:
							signVerifyStatus = (byte)3;
							return SW_USER_CANCEL;
							
					}
				}

				Util.arrayCopyNonAtomic(largeTmpBuf, (short)0, apduBuf, (short)(offset+ISO7816.OFFSET_CDATA), apduLen);// recovery data					
				
				signVerifyStatus = (byte)0;
				if (apduBuf[(short)(offset+ISO7816.OFFSET_CDATA+hashNumOff)] > HASH_NUM_SIZE)
					return ISO7816.SW_DATA_INVALID;
				if (apduLen != (short)(hashNumOff+1))
					return ISO7816.SW_WRONG_LENGTH;
				if ((C = HDUser.read(R, apduBuf, (short)(offset+ISO7816.OFFSET_CDATA))) == null)
					return SW_HD_USER_NO_EXIST;
				
				short offEnd = (short)(offset+ISO7816.OFFSET_CDATA+USER_ID_SIZE+COMPLETE_USER_PATH_SIZE);
				deriveKey(apduBuf, (short)(offset+ISO7816.OFFSET_CDATA+USER_ID_SIZE), offEnd, tempBuf, (short)0, tempBuf, (short)(KEY_SECRET_SIZE+CHAIN_CODE_SIZE));
				
				hashNum = apduBuf[(short)(offset+ISO7816.OFFSET_CDATA+hashNumOff)];				
				break;
			case 1:
				if(signVerifyStatus == (byte)1)  // Process SignData verifying PIN
					return SW_SIGN_VERIFYING;
				else if(signVerifyStatus == (byte)2) // SignData verifying PIN Error
					return SW_VERIFY_FAILD;
				else if(signVerifyStatus == (byte)3) // User cancel Verifying PIN
					return SW_USER_CANCEL;
				begin = (short)((P2 >> 4) & (byte)0x0F);
				end = (short)(P2 & (byte)0x0F);
				
				if ((begin > end) || ((short)(end-begin) >= (short)7) || (end >= hashNum))
					return ISO7816.SW_INCORRECT_P1P2;
				
				if (apduLen != (short) ((short)(end-begin+1)*HASH_VALUE_SIZE))
					return ISO7816.SW_WRONG_LENGTH;
				
				Util.arrayCopyNonAtomic(apduBuf, (short)(offset+ISO7816.OFFSET_CDATA), tempBuf, (short)(HASH_VALUE_SIZE*begin), apduLen);				
				break;
			case 2:
				begin = (short)((P2 >> 4) & (byte)0x0F);
				end = (short)(P2 & (byte)0x0F);
				if ((begin > end) || ((short)(end-begin) >= (short)3) || (end >= hashNum))
					return ISO7816.SW_INCORRECT_P1P2;

				signature.init(privateKey, Signature.MODE_SIGN);

				short sigsSize = 0;
				for (short i = begin; i <= end; i++) {
					signature.signPreComputedHash(tempBuf, (short)(HASH_VALUE_SIZE*i), HASH_VALUE_SIZE, apduBuf, sigsSize);
					
					simomeTool.decodeAsn1(apduBuf, sigsSize);
					sigsSize += (short)(KEY_SECRET_SIZE * 2);
				}
				
				return (short)(0x6100 | sigsSize);				
		}
		preSignStatus = P1;
			
		return SW_STATUS_NO_ERROR;
	}
	
	private short synchWalletInfo(byte[] apduBuf, short offset, short apduLen){
		if (P1 > (byte)5)
			return ISO7816.SW_INCORRECT_P1P2;
		
		switch(P1){ // Synch
			case 1: //Show Mnemonic
				if (apduLen != USER_ID_SIZE)
					return ISO7816.SW_WRONG_LENGTH;

				if ((C = HDUser.read(R, apduBuf, (short)(offset+ISO7816.OFFSET_CDATA))) == null)
					return SW_HD_USER_NO_EXIST;
		
				if((sw = verifyPayCode_STK()) == SW_STATUS_NO_ERROR)
					showMnemonic_STK();
				else 
					return sw;
				break;
			case 2: //Change Transaction PIN
				if((sw = verifyPayCode_STK()) == SW_STATUS_NO_ERROR){
					creatPayCode_STK();
					short itemLen = getWording(tempBuf, (short)0, (short)18, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //PIN Changed
					sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
				}	
				else 
					return sw;
				break;
			case 3: //Get Token
				if(Lc != (short)0x000A) 
					return ISO7816.SW_WRONG_LENGTH;
				short length = (short)256;
				random.generateData(tempBuf, (short)0, (short)256);
				return (short)(0x6100 | length);
			case 4: //Change Language 
				if(Lc != (short)1) 
					return ISO7816.SW_WRONG_LENGTH;
				if(apduBuf[(short)(offset+ISO7816.OFFSET_CDATA)] < (byte)1 && apduBuf[(short)(offset+ISO7816.OFFSET_CDATA)] > (byte)4)
					return SW_LANGUAGE_NO_EXIST;
				menuLanguage = (byte)(apduBuf[(short)(offset+ISO7816.OFFSET_CDATA)] - 1);
				break;
			case 5: //unblock PIN
				return unblockPayCode_STK();
		}
		return SW_STATUS_NO_ERROR;
		
	}

	private void creatWallet_STK(byte[] creatWalletBuf, short offset){
		short cnt;
		short itemLen;
		short tempLen;
		itemLen = getWording(tempBuf, (short)0, (short)0, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //These mnemonic words are used to restore your wallet, they must be in the correct order
		sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
		
		
		for(cnt = (short)0; cnt < (short)12; cnt+=(short)3){		
			itemLen = getWording(tmpRAM, (short)0, (short)1, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Your Mnemonic words:
			if(DCS[menuLanguage] == (byte)0x04){
				if(cnt < (short)9){
					tmpRAM[itemLen++] = (byte)((cnt+1) | 0x30);
					tmpRAM[itemLen++] = (byte)0x2D;
					tmpRAM[itemLen++] = (byte)((cnt+3) | 0x30);
					tmpRAM[itemLen++] = (byte)0x0A;
				}else{
					tmpRAM[itemLen++] = (byte)((byte)((byte)(cnt+1) / 10) | 0x30);
					tmpRAM[itemLen++] = (byte)(((cnt+1) - 10) | 0x30);
					tmpRAM[itemLen++] = (byte)0x2D;
					tmpRAM[itemLen++] = (byte)((byte)((byte)(cnt+3) / 10) | 0x30);
					tmpRAM[itemLen++] = (byte)(((cnt+3) - 10) | 0x30);
					tmpRAM[itemLen++] = (byte)0x0A;
				}
				itemLen += searchWordList(tmpRAM, itemLen, mnemonicCode[cnt]);
				tmpRAM[itemLen++] = (byte)0x0A;
				itemLen += searchWordList(tmpRAM, itemLen, mnemonicCode[(short)(cnt+1)]);
				tmpRAM[itemLen++] = (byte)0x0A;			
				itemLen += searchWordList(tmpRAM, itemLen, mnemonicCode[(short)(cnt+2)]);		
			}else{
				tempLen = (short)0;
				if(cnt < (short)9){
					tempBuf[tempLen++] = (byte)((cnt+1) | 0x30);
					tempBuf[tempLen++] = (byte)0x2D;
					tempBuf[tempLen++] = (byte)((cnt+3) | 0x30);
					tempBuf[tempLen++] = (byte)0x0A;
				}else{
					tempBuf[tempLen++] = (byte)((byte)((byte)(cnt+1) / 10) | 0x30);
					tempBuf[tempLen++] = (byte)(((cnt+1) - 10) | 0x30);
					tempBuf[tempLen++] = (byte)0x2D;
					tempBuf[tempLen++] = (byte)((byte)((byte)(cnt+3) / 10) | 0x30);
					tempBuf[tempLen++] = (byte)(((cnt+3) - 10) | 0x30);
					tempBuf[tempLen++] = (byte)0x0A;
				}
				itemLen += simomeTool.convertAsciiToUcsii(tempBuf, (short)0, tmpRAM, itemLen, tempLen);
				tempLen = searchWordList(tempBuf, (short)1, mnemonicCode[cnt]);
				itemLen += simomeTool.convertAsciiToUcsii(tempBuf, (short)1, tmpRAM, itemLen, tempLen);
				tmpRAM[itemLen++] = (byte)0x00;
				tmpRAM[itemLen++] = (byte)0x0A;
				tempLen = searchWordList(tempBuf, (short)1, mnemonicCode[(short)(cnt+1)]);
				itemLen += simomeTool.convertAsciiToUcsii(tempBuf, (short)1, tmpRAM, itemLen, tempLen);
				tmpRAM[itemLen++] = (byte)0x00;
				tmpRAM[itemLen++] = (byte)0x0A;			
				tempLen = searchWordList(tempBuf, (short)1, mnemonicCode[(short)(cnt+2)]);	
				itemLen += simomeTool.convertAsciiToUcsii(tempBuf, (short)1, tmpRAM, itemLen, tempLen);
			}
			sendDisplayText((byte)0x81, DCS[menuLanguage], tmpRAM, (short)0, itemLen);	
		}
		
		
		
		for(cnt = (short)1; cnt <= (short)12; cnt++){
			itemLen = getWording(tempBuf, (short)0, (short)4, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // Enter Mnemonic Word (12 total), 1
			if(DCS[menuLanguage] == (byte)0x04){
				if(cnt < (short)10){
					tempBuf[(short)(itemLen-1)] = (byte)(cnt + 0x30);
				}else{
					tempBuf[(short)(itemLen-2)] = (byte)((byte)(cnt / 10) + 0x30);
					tempBuf[(short)(itemLen-1)] = (byte)((byte)(cnt % 10) + 0x30);
				}				
			}else{
				if(cnt < (short)10){
					tempBuf[(short)(itemLen-1)] = (byte)(cnt + 0x30);
				}else{
					tempBuf[(short)(itemLen-3)] = (byte)((byte)(cnt / 10) + 0x30);
					tempBuf[(short)(itemLen-1)] = (byte)((byte)(cnt % 10) + 0x30);
				}				
			}

			itemLen = getInput((byte)0x01 ,DCS[menuLanguage], tempBuf, itemLen, (short)3, (short)8, inputBuf, (short)1);	
			inputBuf[0] = (byte)itemLen;
			simomeTool.toLowerCase(inputBuf, (short)1, (short)(inputBuf[0] & 0xFF));
			itemLen = searchWordList(tempBuf, (short)9, mnemonicCode[(short)(cnt-1)]);
			if(((inputBuf[0] != itemLen)) || (Util.arrayCompare(tempBuf, (short)9, inputBuf, (short)1, (short)(inputBuf[0] & 0x00FF)) != (byte)0)){
				itemLen = getWording(tempBuf, (short)0, (short)2, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Incorrect. Your Mnemonic words:
				if(DCS[menuLanguage] == (byte)0x04){
					Util.arrayCopyNonAtomic(creatWalletBuf, (short)(offset+1), tempBuf, itemLen, (short)(creatWalletBuf[offset] & 0xFF));
					itemLen += (short)(creatWalletBuf[offset] & 0xFF);
				}else{
					itemLen += simomeTool.convertAsciiToUcsii(creatWalletBuf, (short)(offset+1), tempBuf, itemLen, (short)(creatWalletBuf[offset] & 0xFF));
				}
				sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
				cnt--;
			}
		}
		itemLen = getWording(tempBuf, (short)0, (short)3, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Creating wallet may take a few minutes, press next to continue
		sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
		
	}
	
	private boolean importWallet_STK(byte[] creatWalletBuf, short offset){
		short cnt;
		short ofs = offset;
		short itemLen;
		creatWalletBuf[ofs++] = (byte)0;

		
		
		for(cnt = (short)1; cnt <= (short)12; cnt++){
			itemLen = getWording(tempBuf, (short)0, (short)4, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // Enter Mnemonic Word (x of 12)
			if(DCS[menuLanguage] == (byte)0x04){
				if(cnt < (short)10){
					tempBuf[(short)(itemLen-1)] = (byte)(cnt + 0x30);
				}else{
					tempBuf[(short)(itemLen-2)] = (byte)((byte)(cnt / 10) + 0x30);
					tempBuf[(short)(itemLen-1)] = (byte)((byte)(cnt % 10) + 0x30);
				}				
			}else{
				if(cnt < (short)10){
					tempBuf[(short)(itemLen-1)] = (byte)(cnt + 0x30);
				}else{
					tempBuf[(short)(itemLen-3)] = (byte)((byte)(cnt / 10) + 0x30);
					tempBuf[(short)(itemLen-1)] = (byte)((byte)(cnt % 10) + 0x30);
				}				
			}
			itemLen = getInput((byte)0x01 , DCS[menuLanguage],tempBuf, itemLen, (short)3, (short)8, inputBuf, (short)1);	
			simomeTool.toLowerCase(inputBuf, (short)1, itemLen);
			
			if(compareWordList(inputBuf, (short)1, itemLen, tempBuf, (short)(cnt -1)) == false){
				itemLen = getWording(tempBuf, (short)0, (short)5, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Not a valid word
				sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
				cnt--;
			}else{
				Util.arrayCopyNonAtomic(inputBuf, (short)1, creatWalletBuf, ofs ,itemLen);
				ofs += itemLen;
				creatWalletBuf[ofs++] = (byte)0x20;
			}
		}
		creatWalletBuf[offset] = (byte)(ofs-offset-2);

		if(validateMnemonic() == false){
			itemLen = getWording(tempBuf, (short)0, (short)6, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Error, please check and try again
			sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
			return false;
		}
		else{
			itemLen = getWording(tempBuf, (short)0, (short)3, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Creating wallet may take a few minutes, press next to continue
			sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
			return true;
		}
	}

	private void creatPayCode_STK(){
		short itemLen;
		short cnt = (short)0;
		short prelength;
		do{
			inputBuf[(short)(SIZE_BUFFER_INPUT- (short)1)] = (byte)0;
			itemLen = getWording(tmpRAM, (short)0, (short)7, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // Enter New Transaction PIN
			itemLen = getInput((byte)0x04 ,DCS[menuLanguage] , tmpRAM, itemLen, (short)4, (short)8, inputBuf, (short)1);
			
			if(pin.check(inputBuf, (short)1, (byte)itemLen)){
				inputBuf[(short)(SIZE_BUFFER_INPUT- (short)1)] = (byte)1;
				itemLen = getWording(tmpRAM, (short)0, (short)21, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // PIN Repeat
				sendDisplayText((byte)0x81, DCS[menuLanguage], tmpRAM, (short)0, itemLen);
			}		
			
		}while(inputBuf[(short)(SIZE_BUFFER_INPUT- (short)1)] == (byte)1);
			
		prelength = itemLen;
		Util.arrayCopyNonAtomic(inputBuf, (short)1, tmpRAM, (short)100, itemLen);
		Util.arrayFillNonAtomic(inputBuf, (short)1, itemLen, (byte)0xFF);
		
		do{
			inputBuf[(short)(SIZE_BUFFER_INPUT- (short)1)] = (byte)0;
			
			itemLen = getWording(tmpRAM, (short)0, (short)8, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // Confirm PIN
			itemLen = getInput((byte)0x04 ,DCS[menuLanguage] , tmpRAM, itemLen, (short)4, (short)8, inputBuf, (short)1);
			
			// check that the two inputs are equal
			if( (prelength != itemLen) || (Util.arrayCompare(inputBuf, (short)1, tmpRAM, (short)100, itemLen) != (byte)0)){
				inputBuf[(short)(SIZE_BUFFER_INPUT- (short)1)] = (byte)1;
				itemLen = getWording(tmpRAM, (short)0, (short)9, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // PIN Mismatch
				sendDisplayText((byte)0x81, DCS[menuLanguage], tmpRAM, (short)0, itemLen);
			}
			
		}while(inputBuf[(short)(SIZE_BUFFER_INPUT- (short)1)] == (byte)1);		
		
		pin.update(inputBuf, (short)1, (byte)itemLen);
	}
	
	private short verifyPayCode_STK(){
				
		short itemLen;
		short resLen;
		short i;
		
		if (isUnBlockedWithPuk == true)
			creatPayCode_STK();
		pinTryCount = pin.getTriesRemaining();
		while(pinTryCount > (byte)0){
			itemLen = getWording(tmpRAM, (short)0, (short)13, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // Enter Transaction PIN
			
			itemLen = getInput((byte)0x04 ,DCS[menuLanguage] , tmpRAM, itemLen, (short)4, (short)8, inputBuf, (short)1);
			
			

			if (pin.check(inputBuf, (short)1, (byte)itemLen)){ 
				return SW_STATUS_NO_ERROR;
			}else{	
				pinTryCount--;
				if(pinTryCount == (byte)0){
					itemLen = getWording(tmpRAM, (short)0, (short)11, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // Wallet will be removed
					sendDisplayText((byte)0x81, DCS[menuLanguage], tmpRAM, (short)0, itemLen);
					// removed all wallet
					resLen = HDUser.readAllUser(R, tempBuf, (short)0);
					for(i = (short)0; i < resLen; i += USER_ID_SIZE){	
						C = HDUser.deleteUser(R, tempBuf, i, tempBuf, resLen);
						if (tempBuf[resLen] == HDUser.ONE_USER){
							R = C;
							C = null;
						}		
					}
				}else if(pinTryCount == (byte)1){
					itemLen = getWording(tmpRAM, (short)0, (short)22, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // The PIN you have entered is incorrect...
					sendDisplayText((byte)0x81, DCS[menuLanguage], tmpRAM, (short)0, itemLen);					
				}else{
					itemLen = getWording(tmpRAM, (short)0, (short)10, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // PIN error
					if(DCS[menuLanguage] == (byte)0x04){
						tmpRAM[itemLen++] = (byte)(pinTryCount + 0x30);
						
					}else{
						tmpRAM[itemLen++] = (byte)0x00;
						tmpRAM[itemLen++] = (byte)(pinTryCount + 0x30);
					}					
					itemLen += getWording(tmpRAM, itemLen, (short)12, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // tries remain 

					sendDisplayText((byte)0x81, DCS[menuLanguage], tmpRAM, (short)0, itemLen);
				}
			}
		}
		
		return unblockPayCode_STK();
	}	
	
	private short unblockPayCode_STK(){
		short itemLen;
		// check unlock try count
		if (unlockTryCount == (short)0)
			return SW_PUK_LOCK;
		unlockTryCount = puk.getTriesRemaining();
		while(unlockTryCount > (short)0){
			itemLen = getWording(tmpRAM, (short)0, (short)19, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // Enter PUK
		
			itemLen = getInput((byte)0x04 ,DCS[menuLanguage] , tmpRAM, itemLen, (short)4, (short)8, inputBuf, (short)1);
					// Check PUK
			if (puk.check(inputBuf, (short)1, (byte)itemLen)) {
				isUnBlockedWithPuk = true;
				unlockTryCount = PUK_ATTEMPT_NUM;

				pin.resetAndUnblock();
				
				itemLen = getWording(tmpRAM, (short)0, (short)23, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // PUK unlocked
				sendDisplayText((byte)0x81, DCS[menuLanguage], tmpRAM, (short)0, itemLen);
				
				creatPayCode_STK();
				return  SW_VERIFY_FAILD;					
			}else{
				unlockTryCount--;
				if (unlockTryCount == (short)0) return SW_PUK_LOCK;
								
				itemLen = getWording(tmpRAM, (short)0, (short)20, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // PUK error
				if(DCS[menuLanguage] == (byte)0x04){
					tmpRAM[itemLen++] = (byte)(unlockTryCount + 0x30);
					
				}else{
					tmpRAM[itemLen++] = (byte)0x00;
					tmpRAM[itemLen++] = (byte)(unlockTryCount + 0x30);
				}					
				itemLen += getWording(tmpRAM, itemLen, (short)12, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); // tries remain 

				sendDisplayText((byte)0x81, DCS[menuLanguage], tmpRAM, (short)0, itemLen);	
			}
		}

		return SW_PUK_LOCK;		
	}
	
	private void showMnemonic_STK(){
		short ofs = (short)0;
		short cnt;
		short itemLen;
		for(cnt = (short)0; cnt < (short)12; cnt++){
			itemLen = searchWordList(tempBuf, (short)9, C.getMnemonicCode(cnt));
			Util.arrayCopyNonAtomic(tempBuf, (short)9, largeTmpBuf, ofs, itemLen);
			ofs += itemLen;
			largeTmpBuf[ofs++] = (byte)0x20;
		}
		
		itemLen = getWording(tempBuf, (short)0, (short)1, FID_DF_COLDWALLET, FID_EF_MENU_WORDING[menuLanguage]); //Your Mnemonic words:
		if(DCS[menuLanguage] == (byte)0x04){
			Util.arrayCopyNonAtomic(largeTmpBuf, (short)0, tempBuf, itemLen, ofs);
			itemLen += ofs;
		}else{
			itemLen += simomeTool.convertAsciiToUcsii(largeTmpBuf, (short)0, tempBuf, itemLen, ofs);
		}

		sendDisplayText((byte)0x81, DCS[menuLanguage], tempBuf, (short)0, itemLen);
	}

}