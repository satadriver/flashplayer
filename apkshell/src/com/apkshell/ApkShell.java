package com.apkshell;

import java.io.ByteArrayOutputStream;  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.security.MessageDigest;  
import java.security.NoSuchAlgorithmException;  
import java.util.zip.Adler32;  

public class ApkShell {
	
	private static String cryptKey = "fuck all the android crackers";
	
	//private static String shellApkPath = "shellApk";
	
    public static void main(String[] args) {
        try {
        	if(args.length < 5){
        		System.out.print("program arguments error\r\n");
        		System.out.print("usage:apkshell.jar shellapk_filename apk_filename path usrename ip \r\n");
        		
        		return;
        	}

        	
        	String shellApkfn = args[0];
        	String srcApkfn = args[1];
        	String unzipPath = args[2];
        	String username = args[3];
        	String ip = args[4];
        	
            if (unzipPath.endsWith("/") == false && unzipPath.endsWith("\\") == false) {
            	unzipPath = unzipPath + "/";
            }
        	
            
        	int cnt = ZipUtils.unZip(shellApkfn,unzipPath);
        	if(cnt <= 0){
        		System.out.println("unzip file:" +shellApkfn + " error");
        		return;
        	}
        	
        	

            File srcApkFile = new File(srcApkfn);
            if(srcApkFile.exists() == false){
            	System.out.println("not found apk file:" + srcApkfn);
            	return;
            }
            
            byte[] apkdata = readFileBytes(srcApkFile);
            int encryptsize = (int)( 4 + username.length() + 4 + ip.length() + srcApkFile.length());
            byte[] enSrcApkArray = new byte[encryptsize];
            
            int offset = 0;
            byte[] byteunamelen = Utils.intToBytes(username.length());
            System.arraycopy(byteunamelen, 0, enSrcApkArray, offset, 4);
            offset += 4;
            
            System.arraycopy(username.getBytes(), 0, enSrcApkArray, offset, username.length());
            offset += username.length();

            byte[] byteiplen = Utils.intToBytes(ip.length());
            System.arraycopy(byteiplen, 0, enSrcApkArray, offset, 4);
            offset += 4;
            
            System.arraycopy(ip.getBytes(), 0, enSrcApkArray, offset, ip.length());
            offset += ip.length();
            
            System.arraycopy(apkdata, 0, enSrcApkArray, offset, apkdata.length);

            xorcrpt(enSrcApkArray);

            //需要解壳的dex以二进制形式读出dex
            String srcdexPath = unzipPath + "classes.dex";
            File unShellDexFile = new File(srcdexPath);
            
            byte[] unShellDexArray = readFileBytes(unShellDexFile);

            //将源APK长度和需要解壳的DEX长度相加并加上存放源APK大小的四位得到总长度
            int enSrcApkLen = enSrcApkArray.length;
            int unShellDexLen = unShellDexArray.length;
            int totalLen = enSrcApkLen + unShellDexLen + 4;
            
            System.out.println("apk size:"+enSrcApkLen+",dex size:" + unShellDexLen +",total size:" + totalLen);
            
            //依次将解壳DEX，加密后的源APK，加密后的源APK大小，拼接出新的Dex
            byte[] newdex = new byte[totalLen];
            System.arraycopy(unShellDexArray, 0, newdex, 0, unShellDexLen);
            System.arraycopy(enSrcApkArray, 0, newdex, unShellDexLen, enSrcApkLen);
            System.arraycopy(intToByte(enSrcApkLen), 0, newdex, totalLen - 4, 4);
            
            //修改DEX file size文件头
            fixFileSizeHeader(newdex);
            //修改DEX SHA1 文件头
            fixSHA1Header(newdex);
            //修改DEX CheckSum文件头
            fixCheckSumHeader(newdex);
  
            //写出新Dex

            File file = new File(srcdexPath);
            if (file.exists() == false) {
                file.createNewFile();
            }  
            
            FileOutputStream fos = new FileOutputStream(srcdexPath);
            fos.write(newdex);
            fos.flush();
            fos.close();
            
            String metainf=unzipPath + "META-INF/";
            File fileMetainf = new File(metainf);
            Utils.deleteDir(fileMetainf);
            
            int counter = ZipUtils.zipDir(shellApkfn + "_new.apk", unzipPath);
            System.out.println("rezip complete total file:" + counter);
            
        } catch (Exception e) {
            e.printStackTrace();
        }  
        return;
    }  
      

    private static byte[] xorcrpt(byte[] srcdata){
    	byte[] key = cryptKey.getBytes();
    	int keylen = cryptKey.length();
        for(int i = 0,j = 0; i<srcdata.length; i++){
            srcdata[i] = (byte)(key[j] ^ srcdata[i]);
            j ++;
            if(j >= keylen){
            	j = 0;
            }
        }
        return srcdata;
    }  
  

  
  
    /** 
     * int 转byte[] 
     * @param number 
     * @return 
     */
    //big endian int,not little endian
    public static byte[] intToByte(int number) {
        byte[] b = new byte[4];
        for (int i = 3; i >= 0; i--) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }
  

  
    /** 
     * 以二进制读出文件内容 
     * @param file 
     * @return 
     * @throws IOException 
     */  
    private static byte[] readFileBytes(File file) throws IOException {  
        byte[] readbuf = new byte[0x1000];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        while (true) {
            int readlen = fis.read(readbuf);
            if (readlen != -1) {
            	baos.write(readbuf, 0, readlen);  
            } else { 
                break;
            }  
        } 
        fis.close();
        return baos.toByteArray();  
    }  
    
    
    /** 
     * 修改dex头 sha1值 
     * @param dexBytes 
     * @throws NoSuchAlgorithmException 
     */  
    private static void fixSHA1Header(byte[] dexBytes) throws NoSuchAlgorithmException {  
        MessageDigest md = MessageDigest.getInstance("SHA-1");  
        md.update(dexBytes, 32, dexBytes.length - 32);
        //从32为到结束计算sha-1  
        byte[] newdt = md.digest();  
        System.arraycopy(newdt, 0, dexBytes, 12, 20);
        //修改sha-1值(12-31) 
        
        //输出sha-1值，可有可无  
        String hexstr = "";  
        for (int i = 0; i < newdt.length; i++) {  
        	//Integer.toString(int i, int radix)将整数i（十进制）转化为radix进制的整数
            hexstr += Integer.toString((newdt[i] & 0xff) + 0x100, 16).substring(1);
        }  
        System.out.println("new dex sha-1:" + hexstr);  
    }  
  
    /** 
     * 修改dex头 file_size值 
     * @param dexBytes 
     */  
    private static void fixFileSizeHeader(byte[] dexBytes) {
        //新文件长度
        byte[] newfs = intToByte(dexBytes.length);  
        
        byte[] refs = new byte[4]; 
        //高位在前 低位在前掉个个
        for (int i = 0; i < 4; i++) {
            refs[i] = newfs[newfs.length - 1 - i];
        }
        
        //修改(32-35)
        System.arraycopy(refs, 0, dexBytes, 32, 4);
        
        System.out.println("new dex file size:" + Integer.toHexString(dexBytes.length));
    }  
    
    /** 
     * 修改dex头，CheckSum 校验码
     * @param dexBytes 
     */  
    private static void fixCheckSumHeader(byte[] dexBytes) {  
        Adler32 adler = new Adler32();  
        adler.update(dexBytes, 12, dexBytes.length - 12);
        //从12到文件末尾计算校验码  
        int value = (int)adler.getValue();  
        byte[] newcs = intToByte(value);  
        //高位在前，低位在前掉个个  
        byte[] recs = new byte[4];  
        for (int i = 0; i < 4; i++) {  
            recs[i] = newcs[newcs.length - 1 - i];  
        }  
        
        //效验码赋值(8-11)
        System.arraycopy(recs, 0, dexBytes, 8, 4);
        
        System.out.println("new dex checksum:" +Integer.toHexString(value));
    }  
    
    
}  


