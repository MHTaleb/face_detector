/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author taleb
 */
public class ImageService {

    public static void deleteFolderContentOrCreate(File folder) throws IOException {
        if(folder.exists())
        {
            FileUtils.cleanDirectory(folder);
        }else{
            folder.mkdir();
        }
    }
}
