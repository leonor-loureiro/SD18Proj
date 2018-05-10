package pt.ulisboa.tecnico.sdis.kerby;

import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.viewToXMLBytes;
import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.xmlBytesToView;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;

/**
 * Class containing several Security related helper methods.
 * */
public class SecurityHelper {

	private static final String CIPHER_ALGO = "AES";
	private static final int CIPHER_KEY_SIZE = 128;
	
	private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final int KDF_ITERATIONS = 2048;
	
	private static final String DEFAULT_SALT = "F2BdmZEp8q";
	
	

	// keys ------------------------------------------------------------------

	/** Generates a key using default parameters. */
	public static Key generateKey() throws NoSuchAlgorithmException {
		return generateKey(CIPHER_ALGO, CIPHER_KEY_SIZE);
	}

	/** Generates a key with the given parameters.
	 * @param algorithm The Algorithm used to generate the key.
	 * @param keySize The Size of the key to generate. */
	private static Key generateKey(String algorithm, int keySize) throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
		keyGen.init(keySize);
		Key key = keyGen.generateKey();
		return key;
	}
	
	/** Generates a Key from a Password using a Key Derivation Function and a Default Salt.
	 * @param password The Password to use. */
	public static Key generateKeyFromPassword(String password) 
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		return generateKeyFromPassword(password, DEFAULT_SALT);
	}
	
	/** Generates a Key from a Password and a Salt using a Key Derivation Function.
	 * @param password The Password to use.
	 * @param salt The Salt to use. */
	public static Key generateKeyFromPassword(String password, String salt) 
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		char[] passwordCharArray = password.toCharArray();
		byte[] saltByteArray = salt.getBytes(); 
		PBEKeySpec spec = new PBEKeySpec(passwordCharArray, saltByteArray, KDF_ITERATIONS, CIPHER_KEY_SIZE);
		SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
		SecretKey tmp = skf.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), CIPHER_ALGO);
        return secret;
	}
	

	public static Key recodeKey(byte[] encodedKey) {
		return new SecretKeySpec(encodedKey, CIPHER_ALGO);
	}

	// ciphers ---------------------------------------------------------------

	private static Cipher initCipher(int opmode, Key key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
		cipher.init(opmode, key);
		return cipher;
	}

	public static Cipher initCipher(Key key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		return initCipher(Cipher.ENCRYPT_MODE, key);
	}

	public static Cipher initDecipher(Key key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		return initCipher(Cipher.DECRYPT_MODE, key);
	}

	// ciphered views ----------------------------------------------------------
	/** Ciphers a View with a Key. */
	public static <V> CipheredView cipher(Class<V> viewClass, V view, Key key) throws KerbyException {
		byte[] plainBytes = null;
		try {
			// Use the type name without package as the XML tag name for the data.
			QName tagName = new QName(viewClass.getSimpleName());
			plainBytes = viewToXMLBytes(viewClass, view, tagName);
		} catch (Exception e) {
			throw new KerbyException("Exception while serializing view!", e);
		}

		try {
			Cipher cipher = initCipher(key);
			byte[] cipherBytes = cipher.doFinal(plainBytes);

			CipheredView cipheredView = new CipheredView();
			cipheredView.setData(cipherBytes);
			return cipheredView;

		} catch (Exception e) {
			throw new KerbyException("Exception while ciphering view!", e);
		}
	}

	/** Deciphers a View with a Key. */
	public static <V> V decipher(Class<V> viewClass, CipheredView cipheredView, Key key) throws KerbyException {
		byte[] newPlainBytes = null;
		try {
			Cipher decipher = initDecipher(key);
			newPlainBytes = decipher.doFinal(cipheredView.getData());
		} catch (Exception e) {
			throw new KerbyException("Exception while deciphering view!", e);
		}

		try {
			return xmlBytesToView(viewClass, newPlainBytes);
		} catch (Exception e) {
			throw new KerbyException("Exception while deserializing view!", e);
		}
	}

}
