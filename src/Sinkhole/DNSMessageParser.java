package Sinkhole;

public class DNSMessageParser {
    private final byte[] data;

    public DNSMessageParser(byte[] data) {
        this.data = data;
    }

    public String extractHostname() {
        int headerLength = 12; // DNS header length
        int questionSectionStart = findQuestionSectionStart(headerLength);
        return decodeDomainName(questionSectionStart);
    }


    private int getDomainNameLength(int offset) {
        int length = 0;
        int index = offset;
        int labelLength;
        while ((labelLength = data[index]) != 0) {
            if ((labelLength & 0xC0) == 0xC0) {
                // Pointer to compressed label
                index = ((labelLength & 0x3F) << 8) | (data[index + 1] & 0xFF);
                continue;
            }
            // Add label length and the length byte itself
            length += labelLength + 1;
            index += labelLength + 1;
        }
        // Add 1 for the null terminator
        return length + 1;
    }

    public String extractQueryType() {
        int headerLength = 12; // DNS header length
        int questionSectionStart = findQuestionSectionStart(headerLength);
        int queryTypeOffset = questionSectionStart + getDomainNameLength(questionSectionStart) + 4; // Offset of 4 bytes for Query Type field
        int queryType = (data[queryTypeOffset] << 8) | (data[queryTypeOffset + 1] & 0xFF);
    
        // Check if the query type is for an IPv4 address (A) or IPv6 address (AAAA)
        if (queryType == 1) {
            // Query type is for an IPv4 address (A)
            return "A";
        } else if (queryType == 28) {
            // Query type is for an IPv6 address (AAAA)
            return "AAAA";
        } else {
            // Unknown query type
            return "Unknown";
        }
    }
    
    
    

    private int findQuestionSectionStart(int headerLength) {
        int index = headerLength;
        int remainingLength = data.length - headerLength;
        int labelLength;
        while (remainingLength > 0 && (labelLength = data[index]) != 0) {
            if ((labelLength & 0xC0) == 0xC0) {
                index = (data[index] & 0x3F) << 8 | (data[index + 1] & 0xFF);
                break;
            }
            index += labelLength + 1;
            remainingLength -= labelLength + 1;
        }
        return index + 5; // Skip the null byte and move to the type and class
    }

    private String decodeDomainName(int offset) {
        StringBuilder domainNameBuilder = new StringBuilder();
        int index = offset;
        int labelLength;
        while ((labelLength = data[index]) != 0) {
            if ((labelLength & 0xC0) == 0xC0) {
                // compressed label pointer
                index = (data[index] & 0x3F) << 8 | (data[index + 1] & 0xFF);
                continue;
            }
            // append label to domain name
            if (domainNameBuilder.length() > 0) {
                domainNameBuilder.append('.');
            }
            domainNameBuilder.append(new String(data, index + 1, labelLength));
            index += labelLength + 1;
        }
        return domainNameBuilder.toString();
    }

    private int decodeDomainNameLength(int offset) {
        int index = offset;
        int labelLength;
        int length = 0;
        while ((labelLength = data[index]) != 0) {
            if ((labelLength & 0xC0) == 0xC0) {
                // compressed label pointer
                index = (data[index] & 0x3F) << 8 | (data[index + 1] & 0xFF);
                continue;
            }
            length += labelLength + 1;
            index += labelLength + 1;
        }
        return length + 1; // Add 1 for the null byte
    }

    public DNSheader extractHeader() {
        short identifier = (short) ((data[0] << 8) | (data[1] & 0xFF));
        short flags = (short) ((data[2] << 8) | (data[3] & 0xFF));
        short questionCount = (short) ((data[4] << 8) | (data[5] & 0xFF));
        short answerCount = (short) ((data[6] << 8) | (data[7] & 0xFF));
        short authorityCount = (short) ((data[8] << 8) | (data[9] & 0xFF));
        short additionalCount = (short) ((data[10] << 8) | (data[11] & 0xFF));

        DNSheader.Builder headerBuilder = new DNSheader.Builder();
        headerBuilder.setIdentifier(identifier)
                     .setFlags(flags)
                     .setQuestionCount(questionCount)
                     .setAnswerCount(answerCount)
                     .setAuthorityCount(authorityCount)
                     .setAdditionalCount(additionalCount);

        return headerBuilder.build();
    }
}
