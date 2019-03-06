#!/usr/bin/env bash

# Call this script to generate self-signed certificates for API Management

# VARS
# Environment variables provided by docker-compose: $ENDPOINT and $TRUSTSTORE_KEYSTORE_PASSWORD
OUTDIR="/configs/self-signed-certs"
OUTDIR_SINGLE_KEYSTORES="$OUTDIR/single-host-keystores"


# Check if all files are created
check_success(){
if [[ -f "$OUTDIR"/apiman.jks || -f "$OUTDIR"/apiman.p12 || -f "$OUTDIR"/tls.crt || -f "$OUTDIR"/tls.key ]]; then
    printf "\nSelf signed certificates successfully created, see 'configs/self-signed-certs'. Please replace the example structure in your configs folder with the certificates from 'configs/self-signed-certs'\n"
    exit 0
else
    printf "Exiting because of errors\n"
    exit 1
fi
}


# Add certificates to keystore, call generate_certificate CN SECRET ALIAS
generate_certificate(){
    # Add all certificates in apiman.jks
    keytool -genkeypair -keystore "$OUTDIR"/apiman.jks -dname "CN=$1, O=Snake Oil" -ext SAN=dns:$1 -keyalg RSA -alias $3 -keypass $2 -storepass $2
    # Create for each certificate an additional keystore
    keytool -genkeypair -keystore "$OUTDIR_SINGLE_KEYSTORES"/"$1".jks -dname "CN=$1, O=Snake Oil" -ext SAN=dns:$1 -keyalg RSA -alias $3 -keypass $2 -storepass $2
}


# Check if output directories already exits, if not create them
if [[ -d "$OUTDIR" ]]; then
    printf "The 'configs/self-signed-certs' directory already contains files. Please remove this manually and run the script again.\n"
    exit 1
else
    printf "This script will generate self-signed certificates based on the configuration you made in the .env file.\n"

    mkdir "$OUTDIR"
    mkdir "$OUTDIR_SINGLE_KEYSTORES"

    # Create keystore
    generate_certificate $ENDPOINT $TRUSTSTORE_KEYSTORE_PASSWORD apimancert
    # Transfrom to PKCS12
    keytool -importkeystore -srckeystore "$OUTDIR"/apiman.jks -destkeystore "$OUTDIR"/apiman.p12 -deststoretype PKCS12 -srcstorepass $TRUSTSTORE_KEYSTORE_PASSWORD -deststorepass $TRUSTSTORE_KEYSTORE_PASSWORD
    # Export tls.key
    openssl pkcs12 -in "$OUTDIR"/apiman.p12 -passin pass:$TRUSTSTORE_KEYSTORE_PASSWORD -nocerts -nodes -out "$OUTDIR"/tls.key
    # Export tls.crt
    openssl pkcs12 -in "$OUTDIR"/apiman.p12 -passin pass:$TRUSTSTORE_KEYSTORE_PASSWORD -nokeys -out "$OUTDIR"/tls.crt
fi


# Check if additional CNs are provided as parameter
if [[ $# -eq 0 ]]; then
    check_success
else
    for CN in "$@"; do
        generate_certificate $CN $TRUSTSTORE_KEYSTORE_PASSWORD $CN
    done
    check_success
fi
