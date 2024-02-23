package com.aki.modfix.util.fix.extensions;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

public interface IClientConnectionEncryptionExtension {
    void setupEncryption(SecretKey key) throws GeneralSecurityException;
}
