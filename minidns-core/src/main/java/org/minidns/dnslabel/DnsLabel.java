/*
 * Copyright 2015-2018 the original author or authors
 *
 * This software is licensed under the Apache License, Version 2.0,
 * the GNU Lesser General Public License version 2 or later ("LGPL")
 * and the WTFPL.
 * You may choose either license to govern your use of this software only
 * upon the condition that you accept all of the terms of either
 * the Apache License 2.0, the LGPL 2.1+ or the WTFPL.
 */
package org.minidns.dnslabel;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * A DNS label is an individual component of a DNS name. Labels are usually shown separated by dots.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5890#section-2.2">RFC 5890 § 2.2. DNS-Related Terminology</a>
 * @author Florian Schmaus
 *
 */
public abstract class DnsLabel implements CharSequence {

    /**
     * The maximum length of a DNS label in octets.
     *
     * @see <a href="https://tools.ietf.org/html/rfc1035">RFC 1035 § 2.3.4.</a>
     */
    public static final int MAX_LABEL_LENGTH_IN_OCTETS = 63;

    /**
     * Whether or not the DNS label is validated on construction.
     */
    public static boolean VALIDATE = true;

    public final String label;

    protected DnsLabel(String label) {
        this.label = label;

        if (!VALIDATE) {
            return;
        }

        setBytesIfRequired();
        if (byteCache.length > MAX_LABEL_LENGTH_IN_OCTETS) {
            throw new LabelToLongException(label);
        }
    }

    private transient String internationalizedRepresentation;

    public final String getInternationalizedRepresentation() {
        if (internationalizedRepresentation == null) {
            internationalizedRepresentation = getInternationalizedRepresentationInternal();
        }
        return internationalizedRepresentation;
    }

    protected String getInternationalizedRepresentationInternal() {
        return label;
    }

    public final String getLabelType() {
        return getClass().getSimpleName();
    }

    @Override
    public final int length() {
        return label.length();
    }

    @Override
    public final char charAt(int index) {
        return label.charAt(index);
    }

    @Override
    public final CharSequence subSequence(int start, int end) {
        return label.subSequence(start, end);
    }

    @Override
    public final String toString() {
        return label;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof DnsLabel)) {
            return false;
        }
        DnsLabel otherDnsLabel = (DnsLabel) other;
        return label.equals(otherDnsLabel.label);
    }

    @Override
    public final int hashCode() {
        return label.hashCode();
    }

    private transient DnsLabel lowercasedVariant;

    public final DnsLabel asLowercaseVariant() {
        if (lowercasedVariant == null) {
            String lowercaseLabel = label.toLowerCase(Locale.US);
            lowercasedVariant = DnsLabel.from(lowercaseLabel);
        }
        return lowercasedVariant;
    }

    private transient byte[] byteCache;

    private void setBytesIfRequired() {
        if (byteCache == null) {
            byteCache = label.getBytes();
        }
    }

    public final void writeToBoas(ByteArrayOutputStream byteArrayOutputStream) {
        setBytesIfRequired();

        byteArrayOutputStream.write(byteCache.length);
        byteArrayOutputStream.write(byteCache, 0, byteCache.length);
    }

    public static DnsLabel from(String label) {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Label is null or empty");
        }

        if (LdhLabel.isLdhLabel(label)) {
            return LdhLabel.fromInternal(label);
        }

        return NonLdhLabel.fromInternal(label);
    }

    public static DnsLabel[] from(String[] labels) {
        DnsLabel[] res = new DnsLabel[labels.length];

        for (int i = 0; i < labels.length; i++) {
            res[i] = DnsLabel.from(labels[i]);
        }

        return res;
    }

    public static boolean isIdnAcePrefixed(String string) {
        return string.toLowerCase(Locale.US).startsWith("xn--");
    }

    public static class LabelToLongException extends IllegalArgumentException {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public final String label;

        LabelToLongException(String label) {
            this.label = label;
        }
    }
}