/**
	GPL3.0 License

	Copyright (c) [2022] [TAISYS TECHNOLOGIES CO., LTD.]

	This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

	You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package com.taisys.Slimduet.Applet;

import javacard.framework.Util;
import uicc.toolkit.ProactiveHandler;
import uicc.toolkit.ProactiveHandlerSystem;
import uicc.toolkit.ProactiveResponseHandler;
import uicc.toolkit.ProactiveResponseHandlerSystem;
import uicc.toolkit.ToolkitException;
public class simomeTool{
	private static byte[] base64MappingTable = {
		(byte)0x41,(byte)0x42,(byte)0x43,(byte)0x44,(byte)0x45,(byte)0x46,(byte)0x47,(byte)0x48,(byte)0x49,(byte)0x4a,(byte)0x4b,(byte)0x4c,(byte)0x4d,(byte)0x4e,(byte)0x4f,(byte)0x50,(byte)0x51,
		(byte)0x52,(byte)0x53,(byte)0x54,(byte)0x55,(byte)0x56,(byte)0x57,(byte)0x58,(byte)0x59,(byte)0x5a,(byte)0x61,(byte)0x62,(byte)0x63,(byte)0x64,(byte)0x65,(byte)0x66,(byte)0x67,(byte)0x68,
		(byte)0x69,(byte)0x6a,(byte)0x6b,(byte)0x6c,(byte)0x6d,(byte)0x6e,(byte)0x6f,(byte)0x70,(byte)0x71,(byte)0x72,(byte)0x73,(byte)0x74,(byte)0x75,(byte)0x76,(byte)0x77,(byte)0x78,(byte)0x79,
		(byte)0x7a,(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,(byte)0x39,(byte)0x2d,(byte)0x5f};
	
	public static short convertHexToBase64Url(byte[] src, short srcOfs, short length, byte[] des, short desOfs){
		byte temp1, temp2, temp3;
		short remainder = (short)(length % 3);
		short i, j;
		length -= remainder;
		for(i = (short)0; i < length; i += (short)3){
			temp1 = src[(short)(i + srcOfs)	   ]; 
			temp2 = src[(short)(i + srcOfs + 1)]; 
			temp3 = src[(short)(i + srcOfs + 2)]; 
			j = (short)(i / 3 * 4); 
			des[(short)(j + desOfs)    ] = base64MappingTable[(short)((temp1 >> 2) & 0x3F)];
			des[(short)(j + desOfs + 1)] = base64MappingTable[(short)((byte)((temp1 << 4 ) & 0x30) | (byte)((temp2 >> 4) & 0x0F))];
			des[(short)(j + desOfs + 2)] = base64MappingTable[(short)((byte)((temp2 << 2 ) & 0x3C) | (byte)((temp3 >> 6) & 0x03))];
			des[(short)(j + desOfs + 3)] = base64MappingTable[(short)(temp3 & 0x3F)];
		} 
		j = (short)(i / 3 * 4); 
		if(remainder == (short)0){
			return j;
		}
		else if(remainder == (short)1){
			temp1 = src[(short)(i + srcOfs)	  ]; 
			temp2 =        (byte)0x00	   	   ;
			des[(short)(j + desOfs)	   ] = base64MappingTable[(short)((temp1 >> 2) & 0x3F)];
			des[(short)(j + desOfs + 1)] = base64MappingTable[(short)((byte)((temp1 << 4 ) & 0x30) | (byte)((temp2 >> 4) & 0x0F))];
		}else if(remainder == (short)2){
			temp1 = src[(short)(i + srcOfs)	   ]; 
			temp2 = src[(short)(i + srcOfs + 1)]; 
			temp3 =        (byte)0x00	   		;
			des[(short)(j + desOfs)	   ] = base64MappingTable[(short)((temp1 >> 2) & 0x3F)];
			des[(short)(j + desOfs + 1)] = base64MappingTable[(short)((byte)((temp1 << 4 ) & 0x30) | (byte)((temp2 >> 4) & 0x0F))];
			des[(short)(j + desOfs + 2)] = base64MappingTable[(short)((byte)((temp2 << 2 ) & 0x3C) | (byte)((temp3 >> 6) & 0x03))];
		}
		return (short)(j + remainder + 1);
	}
		
	// Convert Hex to ASCII
	public static void convertHexToAscii(byte[] src, short srcOfs, byte[] des, short desOfs, short len){
		short cnt = (short)0;
		byte tmp = (byte)0;
		
		for(cnt=(short)0; cnt<len; cnt++){
			// Transfer 1st nibbles
			tmp = (byte)((byte)(src[(short)(cnt+srcOfs)]>>4)&0x0F);
			if(tmp>=(byte)0 && tmp<=(byte)9)  // Handle 0, 1, 2, ...., 9
				des[(short)(2*cnt + desOfs)] = (byte)(tmp + (byte)0x30);
			else                              // Handle A, B, C, ...., F
				des[(short)(2*cnt + desOfs)] = (byte)(tmp - (byte)0x0A + (byte)'A');
			// Transfer 2nd nibbles
			tmp = (byte)(src[(short)(cnt+srcOfs)]&0x0F);
			if(tmp>=(byte)0 && tmp<=(byte)9)  // Handle 0, 1, 2, ...., 9
				des[(short)(2*cnt + (short)1 + desOfs)] = (byte)(tmp + (byte)0x30);
			else                              // Handle A, B, C, ...., F
				des[(short)(2*cnt + (short)1 + desOfs)] = (byte)(tmp - (byte)0x0A + (byte)'A');
		}
	}
	
	// Convert ASCII to Hex
	public  static void convertAsciiToHex(byte[] src, short srcOfs, byte[] des, short desOfs, short len){
		short cnt_1 = (short)0, cnt_2 = (short)0;
		byte subValue = (byte)0;
		
		do{
			for(cnt_2=(short)0; cnt_2<(short)2; cnt_2++){
				if(src[(short)(cnt_1+srcOfs)] >= (byte)0x30 && src[(short)(cnt_1+srcOfs)]<= (byte)0x39)       // Handle 0, 1, 2, ...., 9
					subValue = (byte)0x30;
				else if(src[(short)(cnt_1+srcOfs)] >= (byte)0x41 && src[(short)(cnt_1+srcOfs)]<= (byte)0x46)  // Handle A, B, C, ...., F
					subValue = (byte)((byte)0x41 - (byte)0x0A);
				else
					return;
				
				if(cnt_2 == (byte)0)  // Transfer 1st nibbles
					des[(short)(cnt_1/2 + desOfs)] = (byte)((byte)((byte)(src[(short)(cnt_1+srcOfs)] - subValue)<<4)&0xF0);
				else                  // Transfer 2nd nibbles
					des[(short)(cnt_1/2 + desOfs)] |= (byte)((byte)(src[(short)(cnt_1+srcOfs)] - subValue)&0x0F);
				
				cnt_1++;
			}
		}while(cnt_1 < len);
	}
	
	// Convert Hex(2bytes) to BCD
	public static short convertShortToBcd(short hexData){
		byte  bcd4 = (byte)(hexData / 1000);
		byte  bcd3 = (byte)((byte)(hexData / 100 ) % 10);
		byte  bcd2 = (byte)((short)(hexData / 10 ) % 10);
		byte  bcd1 = (byte)(hexData % 10);
		
		bcd3 = (byte)((byte)((bcd4<<4) & 0xF0) | bcd3);
		bcd1 = (byte)((byte)((bcd2<<4) & 0xF0) | bcd1);
		
		return (short)javacard.framework.Util.makeShort(bcd3, bcd1);
		
	}
	
	// Convert BCD to ASCII
	public static short convertBcd2Ascii(byte[] src, short srcOfs, byte[] des, short desOfs, short len){
		short cnt = (short)0;
		short vaildLen = (short)0;
		byte tmpResult = (byte)0;
		
		for(cnt=(short)0; cnt<len; cnt++){
			tmpResult = (byte)((src[(short)(cnt+srcOfs)]&0x0F) + (byte)0x30);
			if(tmpResult>(byte)0x2F && tmpResult<(byte)0x3A){
				des[(short)(2*cnt + desOfs)] = tmpResult;
				vaildLen++;
			}
			
			tmpResult = (byte)(((src[(short)(cnt+srcOfs)]>>4)&0x0F) + (byte)0x30);
			if(tmpResult>(byte)0x2F && tmpResult<(byte)0x3A){
				des[(short)(2*cnt + (short)1 + desOfs)] = tmpResult;
				vaildLen++;
			}
		}
		
		return vaildLen;
	}
	
	// Convert ASCII to UCSII
	public static short convertAsciiToUcsii(byte[] src, short srcOfs, byte[] des, short desOfs, short len){
		short cnt = (short)0;
		
		for(cnt=(short)0; cnt<len; cnt++){
			des[(short)(2*cnt + desOfs)] = (byte)0;
			des[(short)(2*cnt + (short)1 + desOfs)] = src[(short)(cnt+srcOfs)];
		}
		return (short)(len * 2);
	}
	
	public static void toLowerCase(byte[]src, short srcOfs, short len){
		short cnt = (short)0;
		for(cnt = (short)0; cnt < len; cnt++){
			if(src[(short)(cnt + srcOfs)] >= (byte)'A' && src[(short)(cnt + srcOfs)] <= 'Z')
				src[(short)(cnt + srcOfs)] |= (byte)0x20; 
		}
	}

	public static short encodeAsn1(byte[] arr, short srcOff, short dstOff) {
	  	short padding = 0;
	  	short lenOff = (short)(dstOff + 1);
		
	  	arr[dstOff] = (byte)0x30;
		dstOff += 2;
		
		// r
		arr[dstOff] = (byte)0x02;
		if ((arr[srcOff] & (byte)0x80) == (byte)0x80) {
			padding++;
			arr[(short)(dstOff+1)] = (byte)0x21;		
			arr[(short)(dstOff+2)] = (byte)0x00;
			dstOff += 3;
		}
		else {
			arr[(short)(dstOff+1)] = (byte)0x20;
			dstOff += 2;
		}
		
		Util.arrayCopyNonAtomic(arr, srcOff, arr, dstOff, Slimduet.KEY_SECRET_SIZE);
		dstOff += Slimduet.KEY_SECRET_SIZE;
		
		// s
		arr[dstOff] = (byte)0x02;
		srcOff += Slimduet.KEY_SECRET_SIZE;
		if ((arr[srcOff] & (byte)0x80) == (byte)0x80) {
			padding++;
			arr[(short)(dstOff+1)] = (byte)0x21;
			arr[(short)(dstOff+2)] = (byte)0x00;
			dstOff += 3;
		}
		else {
			arr[(short)(dstOff+1)] = (byte)0x20;
			dstOff += 2;	
		}
		
		Util.arrayCopyNonAtomic(arr, srcOff, arr, dstOff, Slimduet.KEY_SECRET_SIZE);
		arr[lenOff] = (byte)(Slimduet.KEY_SECRET_SIZE * 2 + 4 + padding);
		
		return (short)(arr[lenOff] + 2);
	}
	  
	public static void decodeAsn1(byte[] arr, short srcOff) {
	  	short dstOff = srcOff;

	  	srcOff += 3;
	  	if (arr[srcOff] == (byte)0x21) {
	  		srcOff += 2;
	  	}
		else {
	  		srcOff ++;
	  	}
	  	Util.arrayCopyNonAtomic(arr, srcOff, arr, dstOff, Slimduet.KEY_SECRET_SIZE);

	  	srcOff += (short)(Slimduet.KEY_SECRET_SIZE + 1);	
	  	if (arr[srcOff] == (byte)0x21) {
	  		srcOff += 2;
	  	}
		else {
	  		srcOff ++;
	  	}
		Util.arrayCopyNonAtomic(arr, srcOff, arr, (short)(dstOff+Slimduet.KEY_SECRET_SIZE), Slimduet.KEY_SECRET_SIZE);
	}	
}