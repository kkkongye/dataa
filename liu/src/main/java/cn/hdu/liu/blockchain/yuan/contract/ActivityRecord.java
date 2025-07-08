package cn.hdu.liu.blockchain.yuan.contract;

import com.thanos.web3j.abi.datatypes.Address;
import com.thanos.web3j.abi.datatypes.Utf8String;

import java.math.BigInteger;

public class ActivityRecord {
    private final Address caller;
    private final BigInteger timestamp;
    private final BigInteger statusCode;
    private final Utf8String objectCode;

    public ActivityRecord(Address caller, BigInteger timestamp, BigInteger statusCode, Utf8String objectCode) {
        this.caller = caller;
        this.timestamp = timestamp;
        this.statusCode = statusCode;
        this.objectCode = objectCode;
    }

    public Address getCaller() { return caller; }
    public BigInteger getTimestamp() { return timestamp; }
    public BigInteger getStatusCode() { return statusCode; }
    public Utf8String getObjectCode() { return objectCode; }

    @Override
    public String toString() {
        return "ActivityRecord{" +
                "caller=" + caller.getValue() +
                ", timestamp=" + timestamp +
                ", statusCode=" + statusCode +
                ", objectCode='" + objectCode.getValue() + '\'' +
                '}';
    }
}
