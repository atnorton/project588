package com.google.android.apps.authenticator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.android.apps.authenticator.Base32String.DecodingException;
import com.google.android.apps.authenticator.PasscodeGenerator.Signer;

public class TOTPUtility {
	public static String getCurrentCode(String secret) throws Exception {
		long otp_state = 0;

		// For time-based OTP, the state is derived from clock.
		TotpCounter mTotpCounter = new TotpCounter(PasscodeGenerator.INTERVAL);
		// otp_state =
		// mTotpCounter.getValueAtTime(Utilities.millisToSeconds(mTotpClock.currentTimeMillis()));
		// otp_state =
		// mTotpCounter.getValueAtTime(Utilities.millisToSeconds(mTotpClock.currentTimeMillis()));
		otp_state = mTotpCounter
				.getValueAtTime(System.currentTimeMillis() / 1000);

		return computePin(secret, otp_state);
	}

	private static Signer getSigningOracle(String secret) {
		try {
			byte[] keyBytes = decodeKey(secret);
			final Mac mac = Mac.getInstance("HMACSHA1");
			mac.init(new SecretKeySpec(keyBytes, ""));

			// Create a signer object out of the standard Java MAC
			// implementation.
			return new Signer() {
				public byte[] sign(byte[] data) {
					return mac.doFinal(data);
				}
			};
		} catch (Exception e) {
			System.err.println("Bad");
		}

		return null;
	}

	private static byte[] decodeKey(String secret) throws DecodingException {
		return Base32String.decode(secret);
	}

	private static String computePin(String secret, long otp_state)
			throws Exception {
		if (secret == null || secret.length() == 0) {
			throw new Exception("Null or empty secret");
		}

		try {
			Signer signer = getSigningOracle(secret);
			PasscodeGenerator pcg = new PasscodeGenerator(signer);

			return pcg.generateResponseCode(otp_state);
		} catch (Exception e) {
			throw new Exception("Crypto failure", e);
		}
	}
}
