# Cold Wallet Sample
This is a sample Applet based on the Java Card standard platform that combines a cold wallet with a BIP. Before using this example, there are some  prerequisites

## Prerequisites
- Card Support: You should use Java Card that supports Java Card framwork version 3.0.4 (or above), globalplatform version 2.2.1 (or above), and UICC API for Java Card
- Server: If you want to use the BIP channel as a communication medium, you should create a Server yourself as the entity that communicates with Java Card
- File system: According to your implementation, you should create your own file system for your Applet to save relevant sensitive data, such as: IP address, key, etc.

## Usage
Convert cap files using any tool that can load and manage applets on JavaCard (e.g [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro))

## File system architecture
The file structure used in this example is:
- MF_3F00
	- DF_BIP
		- EF_CARD_ID
		- EF_BIP_IP
		- EF_BIP_Port
 	- DF_COLDWALLET
 		- EF_MENU_MNEMONICWORD
 		- EF_LanguageName
 		- EF_MenuWording1
 		- EF_MenuWording2
 		- EF_MenuWording3
 		- EF_MenuWording4    

## Notes
The elliptic curve algorithm scalar multiplied by a given point in this example is suitable for SIMoME series cards of Taisys. For details, please refer to [SECP256k1](SECP256k1) 

## License
This work is licensed under MIT License. See [LICENSE](LICENSE.txt) for details.

## Contact Us
Have any problem? You can [click here](https://taisys.com/contact?lang=).