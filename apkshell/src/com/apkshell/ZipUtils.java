package com.apkshell;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
	
	public static int zipDir(String zipfn,String srcpath){
		int cnt = 0;
		try {
			FileOutputStream fout = new FileOutputStream(zipfn);
			ZipOutputStream zos = new ZipOutputStream(fout);
			cnt = compressDir(zos,srcpath,"");
			
			//Exception:Stream closed
			//fout.close();
			
			zos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cnt;
	}
	
	public static int compressDir(ZipOutputStream zos,String srcpath,String basepath) throws Exception
	{
		int cnt = 0;
		try {
			File[] flist = new File(srcpath).listFiles();

			ZipEntry entry = null;
			
	        for(int i=0; i < flist.length; i++)
	        {
	        	String filename = flist[i].getName();
			    if(flist[i].isDirectory())
			    {
		        	entry = new ZipEntry(basepath + filename + "/");
		        	
		        	zos.putNextEntry( entry );

	            	cnt += compressDir(zos,flist[i].getAbsolutePath(),basepath + filename + "/");  
			    }
			    else
			    {
					entry = new ZipEntry(basepath + filename);
					
					zos.putNextEntry( entry );
					
			        FileInputStream fin = new FileInputStream(flist[i]);
			        
			        byte buf[] = new byte[0x10000];

			        while(true)
			        {
			        	int len = fin.read(buf,0,0x10000);
			        	if(len == -1){
			        		break;
			        	}
			        	zos.write(buf,0,len);
			        }

			        fin.close();
			    }
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cnt;
	}
	    
	
	
	public void compressFile(ZipOutputStream zos,String srcpath,String basepath) throws Exception
	{

		File srcpathFile = new File(srcpath);
		if (srcpathFile.exists() == false) {
			return;
		}

    	String filename = srcpathFile.getName();

    	ZipEntry entry = new ZipEntry(basepath + filename);
		
		zos.putNextEntry( entry );
		
        FileInputStream fin = new FileInputStream(srcpath);
        
        byte buf[] = new byte[0x10000];
        
        while(true)
        {
        	int len = fin.read(buf,0,0x10000);
        	if(len == -1){
        		break;
        	}
        	zos.write(buf,0,len);
        }

        fin.close();
	}
	
	
	
	
    public static int unZip(String unZipfileName, String mDestPath) {
    	int cnt = 0;

        FileInputStream fin = null;
        try {
        	fin = new FileInputStream(unZipfileName);
		} catch (Exception e) {
			e.printStackTrace();
			return cnt;
		}
        
        ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(fin));
        
        try {
        	ZipEntry zipEntry = null;
            int readedBytes = 0;
            byte buf[] = new byte[0x4000];
            
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                File file = new File(mDestPath + zipEntry.getName());
                if ( zipEntry.isDirectory()) {
                	if (file.exists() == false) {
                		file.mkdirs();
					}
                } else {
                    // 如果指定文件的目录不存在,则创建
                    File parent = file.getParentFile();
                    if (parent.exists() == false) {
                        parent.mkdirs();
                    }
                    FileOutputStream fileOut = new FileOutputStream(file);
                    while ((readedBytes = zipIn.read(buf)) > 0) {
                        fileOut.write(buf, 0, readedBytes);
                    }
                    fileOut.close();
                    cnt ++;
                }
                zipIn.closeEntry();
            }
            
            zipIn.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return cnt;
    }
}
