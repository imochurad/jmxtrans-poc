package monitoring.util;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustStoreHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TrustStoreHandler.class);
    private static final String TRUST_STORE_PATH = "/app/config/cacerts";
    private static final char[] PASSWORD = "truststore_password".toCharArray();
    
    // @formatter:off
    public static void importCert(String certAsString, String alias) {

        try {
            System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
            InputStream is = new FileInputStream(TRUST_STORE_PATH);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, PASSWORD);
            is.close();

            if (keystore.containsAlias(alias)) {
                LOG.info("Certificate with alias {} was already imported, skipping step", alias);
            } else {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(certAsString.getBytes()));

                keystore.setCertificateEntry(alias, cert);

                FileOutputStream out = new FileOutputStream(TRUST_STORE_PATH);
                keystore.store(out, PASSWORD);
                out.close();

                LOG.info("Successfully imported certificate with alias {} to the trust store", alias);
            }
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            LOG.error(String.format("Error importing certificate with alias [%s]", alias), e);
        }
    }
    // @formatter:on

}
