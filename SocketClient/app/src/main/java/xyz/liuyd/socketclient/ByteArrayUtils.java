package xyz.liuyd.socketclient;

/**
 * Created by silcata on 2016/09/09.
 */
public class ByteArrayUtils {
    public static byte[] intTo4String(int value){
        String result = "";
        int prefixLen = 4 - String.valueOf(value).length();
        for (int i = 0; i < prefixLen; i++){
            result += "0";
        }
        return (result + value).getBytes();
    }
}
