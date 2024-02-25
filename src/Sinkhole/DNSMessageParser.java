package Sinkhole;



public class DNSMessageParser {
    private final byte[] data;

    public DNSMessageParser(byte[] data) {
        this.data = data;
    }

    public String extractHostname() {
        int headerLength = 12; // dns header length
        int questionSectionStart = findQuestionSectionStart(headerLength);
        return decodeDomainName(questionSectionStart);
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
        return index + 1; // Skip the null byte
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
