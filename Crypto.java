/**
	GPL3.0 License

	Copyright (c) [2022] [TAISYS TECHNOLOGIES CO., LTD.]

	This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

	You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package com.taisys.Slimduet.Applet;


import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.*;

/**
 * Crypto utilities, mostly BIP32 related. The init method must be called during application installation. This class
 * is not meant to be instantiated.
 */
public class Crypto {

  // The below 4 objects can be accessed anywhere from the entire applet
  private static MessageDigest sha256;
  private static MessageDigest sha512;

  private static Signature hmacSHA512;
  private static HMACKey hmacKey;

  /**
   * Initializes the objects required by this class. Must be invoked exactly 1 time during application installation.
   */
  public Crypto() {
    sha256 = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
    sha512 = MessageDigest.getInstance(MessageDigest.ALG_SHA_512, false);

    try {
		hmacSHA512 = Signature.getInstance(Signature.ALG_HMAC_SHA_512, false);
		hmacKey = (HMACKey) KeyBuilder.buildKey(KeyBuilder.TYPE_HMAC_TRANSIENT_RESET, KeyBuilder.LENGTH_HMAC_SHA_512_BLOCK_128, false);
	  
    } catch (CryptoException e) {
		hmacSHA512 = null;
    }
  }
  
  static short getSHA256(byte[] inBuff, short inOffset, short inLength, byte[] outBuff, short outOffset){

	sha256.doFinal(inBuff, inOffset, inLength, outBuff, outOffset);

	return sha256.getLength();
  }

  /**
   * Derives a private key according to the algorithm defined in BIP32. The BIP32 specifications define some checks
   * to be performed on the derived keys. In the very unlikely event that these checks fail this key is not considered
   * to be valid so the derived key is discarded and this method returns false.
   *
   * @param i the buffer containing the key path element (a 32-bit big endian integer)
   * @param iOff the offset in the buffer
   * @param privateKey the parent private key
   * @param publicKey the parent public key
   * @param chain the chain code
   * @param chainOff the offset in the chain code buffer
   * @return true if successful, false otherwise
   */
	static boolean bip32CKDPriv(byte[] i, short iOff, byte[] privateKey, short prvOff, byte[] out, short outOff, byte[] chain, short chainOff) {
		short off = 0;

		hmacKey.setKey(chain, chainOff, Slimduet.KEY_SECRET_SIZE);
		hmacSHA512.init(hmacKey, Signature.MODE_SIGN);

		hmacSHA512.update(out, outOff, (short) 33);

		hmacSHA512.sign(i, iOff, (short) 4, out, outOff);

		if (ucmp256(out, outOff, SECP256k1.SECP256K1_R, (short) 0) >= 0) {
		  return false;
		}

		addm256(out, outOff, privateKey, prvOff, SECP256k1.SECP256K1_R, (short) 0, out, outOff);

		if (isZero256(out, outOff)) {
		  return false;
		}

		Util.arrayCopyNonAtomic(out, outOff, privateKey, prvOff, Slimduet.KEY_SECRET_SIZE);
		Util.arrayCopyNonAtomic(out, (short)(outOff + Slimduet.KEY_SECRET_SIZE), chain, chainOff, Slimduet.KEY_SECRET_SIZE);

		return true;
	}


  /**
   * Calculates the HMAC-SHA512 with the given key and data. Uses a software implementation which only requires SHA-512
   * to be supported on cards which do not have native HMAC-SHA512.
   *
   * @param key the HMAC key
   * @param keyOff the offset of the key
   * @param keyLen the length of the key
   * @param in the input data
   * @param inOff the offset of the input data
   * @param inLen the length of the input data
   * @param out the output buffer
   * @param outOff the offset in the output buffer
   */
	static void hmacSHA512(byte[] key, short keyOff, short keyLen, byte[] in, short inOff, short inLen, byte[] out, short outOff) {
	  
		hmacKey.setKey(key, keyOff, keyLen);
		hmacSHA512.init(hmacKey, Signature.MODE_SIGN);
		hmacSHA512.sign(in, inOff, inLen, out, outOff);
	}

  /**
   * Modulo addition of two 256-bit numbers.
   *
   * @param a the a operand
   * @param aOff the offset of the a operand
   * @param b the b operand
   * @param bOff the offset of the b operand
   * @param n the modulo
   * @param nOff the offset of the modulo
   * @param out the output buffer
   * @param outOff the offset in the output buffer
   */
	private static void addm256(byte[] a, short aOff, byte[] b, short bOff, byte[] n, short nOff, byte[] out, short outOff) {
		if ((add256(a, aOff, b, bOff, out, outOff) != 0) || (ucmp256(out, outOff, n, nOff) > 0)) {
		  sub256(out, outOff, n, nOff, out, outOff);
		}
	}

  /**
   * Compares two 256-bit numbers. Returns a positive number if a > b, a negative one if a < b and 0 if a = b.
   *
   * @param a the a operand
   * @param aOff the offset of the a operand
   * @param b the b operand
   * @param bOff the offset of the b operand
   * @return the comparison result
   */
	private static short ucmp256(byte[] a, short aOff, byte[] b, short bOff) {
		short ai, bi;
		for (short i = 0 ; i < 32; i++) {
		  ai = (short)(a[(short)(aOff + i)] & 0x00ff);
		  bi = (short)(b[(short)(bOff + i)] & 0x00ff);

		  if (ai != bi) {
			return (short)(ai - bi);
		  }
		}

		return 0;
	}

  /**
   * Checks if the given 256-bit number is 0.
   *
   * @param a the a operand
   * @param aOff the offset of the a operand
   * @return true if a is 0, false otherwise
   */
	private static boolean isZero256(byte[] a, short aOff) {
		boolean isZero = true;

		for (short i = 0; i < (byte) 32; i++) {
		  if (a[(short)(aOff + i)] != 0) {
			isZero = false;
			break;
		  }
		}

		return isZero;
	}

  /**
   * Addition of two 256-bit numbers.
   *
   * @param a the a operand
   * @param aOff the offset of the a operand
   * @param b the b operand
   * @param bOff the offset of the b operand
   * @param out the output buffer
   * @param outOff the offset in the output buffer
   * @return the carry of the addition
   */
	private static short add256(byte[] a, short aOff,  byte[] b, short bOff, byte[] out, short outOff) {
		short outI = 0;
		for (short i = 31 ; i >= 0 ; i--) {
		  outI = (short) ((short)(a[(short)(aOff + i)] & 0xFF) + (short)(b[(short)(bOff + i)] & 0xFF) + outI);
		  out[(short)(outOff + i)] = (byte)outI ;
		  outI = (short)(outI >> 8);
		}
		return outI;
	}

	/**
	* Subtraction of two 256-bit numbers.
	*
	* @param a the a operand
	* @param aOff the offset of the a operand
	* @param b the b operand
	* @param bOff the offset of the b operand
	* @param out the output buffer
	* @param outOff the offset in the output buffer
	* @return the carry of the subtraction
	*/
	private static short sub256(byte[] a, short aOff,  byte[] b, short bOff, byte[] out, short outOff) {
		short outI = 0;

		for (short i = 31 ; i >= 0 ; i--) {
		  outI = (short)  ((short)(a[(short)(aOff + i)] & 0xFF) - (short)(b[(short)(bOff + i)] & 0xFF) - outI);
		  out[(short)(outOff + i)] = (byte)outI ;
		  outI = (short)(((outI >> 8) != 0) ? 1 : 0);
		}

		return outI;
	}
}
