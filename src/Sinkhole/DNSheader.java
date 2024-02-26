package Sinkhole;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class DNSheader{
    //DNS header fields
    private final short identifier;
    private final short flags;
    private final short questionCount;
    private final short answerCount;
    private final short authorityCount;
    private final short additionalCount;
    public static final int HEADER_LENGTH = 12;

    //Private Constructor to enforce object creation through builder
    private DNSheader(short identifier, short flags, short questionCount, short answerCount, short authorityCount, short additionalCount) {
        this.identifier = identifier;
        this.flags = flags;
        this.questionCount = questionCount;
        this.answerCount = answerCount;
        this.authorityCount = authorityCount;
        this.additionalCount = additionalCount;
    }

   
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH);
        buffer.putShort(identifier);
        buffer.putShort(flags);
        buffer.putShort(questionCount);
        buffer.putShort(answerCount);
        buffer.putShort(authorityCount);
        buffer.putShort(additionalCount);
        return buffer.array();
    }


    public static class Builder {
        private short identifier;
        private short flags;
        private short questionCount;
        private short answerCount;
        private short authorityCount;
        private short additionalCount;

        public Builder setIdentifier(short identifier){
            this.identifier = identifier;
            return this;
        }
        public Builder setFlags( short flags){
            this.flags = flags;
            return this;
        }
        public Builder setQuestionCount(short questionCount){
            this.questionCount = questionCount;
            return this;
        }
        public Builder setAnswerCount(short answerCount){
            this.answerCount = answerCount;
            return this;
        }
        public Builder setAuthorityCount(short authorityCount){
            this.authorityCount = authorityCount;
            return this;
        }
        public Builder setAdditionalCount(short additionalCount){
            this.additionalCount = additionalCount;
            return this;
        }
        public DNSheader build(){
            return new DNSheader(identifier, flags, questionCount, answerCount, authorityCount, additionalCount);
        }
    }

    //Getters for the header fields
    public short getIdentifier(){
        return identifier;
    }
    public short getFlags(){
        return flags;
    }
    public short getQuestionCount(){
        return questionCount;
    }
    public short getAnswerCount(){
        return answerCount;
    }
    public short getAuthorityCount(){
        return authorityCount;
    }
    public short getAdditionalCount(){
        return additionalCount;
    }
}