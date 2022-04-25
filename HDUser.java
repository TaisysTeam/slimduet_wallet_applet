package com.taisys.Slimduet.Applet;


import javacard.framework.*;
import javacard.security.*;

public class HDUser {
	
	private HDUser next;	// points next HDUser
	private ECPrivateKey masterPrivate;	// Root Private
	private byte[] userID;	// UserID
	private byte[] masterChainCode;	// Root ChainCode
	private short[] mnemonicCode;
	
	protected static byte NO_USER = (byte)0;
	protected static byte ONE_USER = (byte)1;
	protected static byte MORE_USER = (byte)2;
	
	public HDUser() {
			
		masterPrivate = (ECPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, KeyBuilder.LENGTH_EC_FP_256, false);
		SECP256k1.setCurveParameters(masterPrivate);
		userID = new byte[Slimduet.USER_ID_SIZE];	
		masterChainCode = new byte[Slimduet.CHAIN_CODE_SIZE];
		mnemonicCode = new short[Slimduet.MNEMONIC_SIZE];
	}
	
	public HDUser getNext() {
		return next;
	}
	
	public ECPrivateKey getMasterPrivate() {
		return masterPrivate;
	}
	
	public byte[] getUserID() {
		return userID;
	}
	
	public byte[] getMasterChainCode() {
		return masterChainCode;
	}
	


	public short getMnemonicCode(short index){
		return mnemonicCode[index];
	}
	
	public void setMnemonicCode(short[] value, short Ofs, byte length){
		short i;
		for(i = (short)0; i < (short)length; i++){
			mnemonicCode[i] = value[Ofs++];
		}
	}
	
	/** Create HD User
	 * @param root Root node of the list, if null, ROOT node will be created
	 * @param id current User index
	 * @remark
	 *   output current nod
	 **/
	static HDUser createUser(HDUser root) {
		HDUser nod = null;
		HDUser lst = null;
		
		nod = new HDUser();

		if (root==null) return nod;
		
		lst = root;
		
		while(lst.next != null){
			lst = lst.next;
		}
		lst.next = nod;
		
		return nod;
	}
	
	/** Read id HD User
	 * @param root Root node of the list, if null, return null
	 * @param id current User index
	 * @remark 
	 *   output current nod
	 **/
	static HDUser read(HDUser root, byte[] id, short ofs) {
		HDUser lst = null;
		
		if (root==null) return null;

		lst = root;
		while(lst != null) {
			if (Util.arrayCompare(id, ofs, lst.userID, (short) 0, Slimduet.USER_ID_SIZE) == 0) {
				return lst;
			}
				
			lst = lst.next;	
		}
		
		return null;
	}
	
	
	/** Read all HD User
	 * @param root Root node of the list, if null, return null
	 * @param outBuf return all User id
	 * @ofs Available offset into out buffer
	 * @remark
	 *    output Available size of out data
	 **/
	static short readAllUser(HDUser root, byte[] outBuf, short ofs) {		
		short size = (short) 0;
		HDUser lst = null;

		if (root==null) return (short) 0;

		lst = root;		
		while(lst != null) {
			Util.arrayCopy(lst.userID, (short) 0, outBuf, (short) (ofs+size), Slimduet.USER_ID_SIZE);
			size += Slimduet.USER_ID_SIZE;		
			lst = lst.next;
		}

		return size;
	}
	
	/** Read all HD User
	 * @param root Root node of the list, if null, return null
	 * @remark
	 *   output Available size of out data
	 **/
	static short readAllUserCount(HDUser root) {		
		short count = (short) 0;
		HDUser lst = null;

		if (root==null) return (short) 0;

		lst = root;		
		while(lst != null) {			
			lst = lst.next;	
			count++;
		}
		
		return count;
	}

	
	/** Delete HD User
	 * @param root Root node of the list, if null, return null
	 * @param id current User index
	 * @remark
	 *   output next nod
	 **/
	static HDUser deleteUser(HDUser root, byte[] id, short idOfs, byte[] buf, short bufOfs) {
		HDUser lst = null;
		HDUser nod = null;
		
		buf[bufOfs] = NO_USER;
		
		if (root==null) return null;
		
		if (Util.arrayCompare(id, idOfs, root.userID, (short) 0, Slimduet.USER_ID_SIZE) == 0) {
			if (JCSystem.isObjectDeletionSupported()) JCSystem.requestObjectDeletion();
			
			buf[bufOfs] = ONE_USER;
			return root.next;
		}

		nod = root;
		while (nod != null) {
			if (Util.arrayCompare(id, idOfs, nod.userID, (short) 0, Slimduet.USER_ID_SIZE) == 0) {	
				lst.next = nod.next;
				if (JCSystem.isObjectDeletionSupported()) JCSystem.requestObjectDeletion();
				
				buf[bufOfs] = MORE_USER;		
				return nod;
			}
			lst = nod;
			nod = nod.next;
		}
		
		return null;
	}
		
}