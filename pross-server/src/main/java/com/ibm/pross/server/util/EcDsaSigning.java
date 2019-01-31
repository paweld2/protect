/*
 * Copyright (c) IBM Corporation 2018. All Rights Reserved.
 * Project name: pross
 * This project is licensed under the MIT License, see LICENSE.
 */

package com.ibm.pross.server.util;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

import com.ibm.pross.common.CommonConfiguration;
import com.ibm.pross.common.util.crypto.kdf.HmacKeyDerivationFunction;
import com.ibm.pross.common.util.serialization.Serialization;
import com.ibm.pross.server.messages.MessageSignature;

public class EcDsaSigning {

	// TODO: Look up how to do RSA-PSS, might need BouncyCastle
	private final static String DEFAULT_ALGORITHM = CommonConfiguration.SIGNATURE_ALGORITHM;

	public static boolean verifySignature(final Serializable message, final MessageSignature signature,
			final PublicKey senderPublicKey) {

		try {
			final Signature signingContext = Signature.getInstance(signature.getAlgorithm(), "BC");
			signingContext.initVerify(senderPublicKey);
			try {

				final byte[] messageBytes = Serialization.serializeClass(message);
				signingContext.update(messageBytes);
				return signingContext.verify(signature.getSignatureBytes());

			} catch (SignatureException e) {
				// Signature check failed
				return false;
			}

		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static MessageSignature createSignature(final Serializable message, final PrivateKey senderSigningKey) {

		// Generate signautres 
		//HMacDSAKCalculator kCalculator = new HMacDSAKCalculator()
		//ECDSASigner signer = new ECDSASigner(kCalculator);
		
		
		try {
			// Create signing context
			final Signature signingContext = Signature.getInstance(DEFAULT_ALGORITHM, "BC");
			signingContext.initSign(senderSigningKey);

			// Serialize message to sign it
			final byte[] messageBytes = Serialization.serializeClass(message);
			signingContext.update(messageBytes);

			// Compute and return signature
			byte[] signatureBytes = signingContext.sign();
			return new MessageSignature(signatureBytes, DEFAULT_ALGORITHM);

		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
			throw new RuntimeException(e);
		}

	}

}