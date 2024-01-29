package ro.usv;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Fisiere {
    public static File downloadFisier(String numefis){
        try{
            URL website = new URL(numefis);
            ReadableByteChannel rbc;
            rbc = Channels.newChannel(website.openStream());
            File fdest = new File(numefis.substring(numefis.lastIndexOf("/")+1));
            FileOutputStream fos = new FileOutputStream(fdest);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            return fdest;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
