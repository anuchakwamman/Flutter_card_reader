package com.example.esignaturecardreader;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import java.util.List;

public class ThaiADPU {

    byte[] select = {
            (byte) 0x00,(byte)0xA4,(byte)0x04,(byte)0x00,(byte)0x08,(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x54,(byte)0x48,(byte)0x00,(byte)0x01
    };

    byte[]  cid = {
            (byte)0x80,
            (byte)0xB0,
            (byte)0x00,
            (byte)0x04,
            (byte)0x02,
            (byte)0x00,
            (byte)0x0D};

    byte[]  cidGetdata =
            {
                    (byte) 0x00,
                    (byte) 0xC0,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x0D
            };

//    public HashMap<String, Object> readAll(Reader r){
//        return readSpecific(r, allDataList);
//    }


//    public  HashMap<String, String> readSpecific(Reader r, List<String> reqList) throws ReaderException {
//        HashMap< String, String> response = new HashMap<>();
//            byte[] respArray = new byte[500];
//            int responsLength;
//            int slotNum = 0;
//            resetCard(r);
//            setProtocol(r);
//            if (reqList.contains("cif")){
//                int cidLenght = cid.length;
//                r.transmit(slotNum, cid, cidLenght, respArray, respArray.length);
//                responsLength =
//                        r.transmit(
//                                slotNum,
//                                cidGetdata,
//                                cidGetdata.length,
//                                respArray,
//                                respArray.length
//                        );
//            String res = byteArrayToHexString(respArray, 0, responsLength);
//                response.put("cid", res);
//
//        }
//            return response;
//    }

    public String readSpecific(Reader r, List<String> reqList) throws ReaderException {
//        HashMap< String, String> response = new HashMap<>();
        byte[] respArray = new byte[500];
        int responsLength = 0;
        int slotNum = 0;
        resetCard(r);
        setProtocol(r);
        if (reqList.contains("cif")){
            int cidLenght = cid.length;
            r.transmit(slotNum, cid, cidLenght, respArray, respArray.length);
            responsLength =
                    r.transmit(
                            slotNum,
                            cidGetdata,
                            cidGetdata.length,
                            respArray,
                            respArray.length
                    );
            String res = byteArrayToHexString(respArray, 0, responsLength);
//            response.put("cid", res);

        }
        return hexToAscii(hex(respArray));
    }

    private void resetCard(Reader r) throws ReaderException {
        r.power(0, Reader.CARD_WARM_RESET);
    }

    private void setProtocol(Reader r) throws ReaderException {
        r.setProtocol(0, Reader.PROTOCOL_T0);
        byte[] response = new byte[300];
        r.transmit(0, select, select.length, response, response.length);
    }

    private String byteArrayToHexString(byte[] input,Integer index,Integer length){
        if (length + index > input.length) {
            length = input.length - index;
        }
        byte[] selectBytes = new byte[length];
        System.arraycopy(input, index, selectBytes, 0, length - 2);
        return showByteString(selectBytes);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
            // upper case
            // result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }


    private String showByteString(byte[] input) {
        StringBuilder output = new StringBuilder();
        for (int i=0;i<input.length;i++) {
            output.append(String.format("%02x", input[i]));
        }
        String result = null;
//        result = input.toString(Charset.forName("TIS620"));
        result = input.toString();
        return result;
    }


}
