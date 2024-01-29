package ro.usv;
import java.io.File;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
public class PDF_Text {
    public static String extrage(String numefis) {
        String st="";
        File fdest=null;
        boolean website=false;
        PDDocument document=null;
        try {
            if(numefis.indexOf("http://")==0 || numefis.indexOf("https://")==0) {
                fdest = Fisiere.downloadFisier(numefis);
                website=true;
            } else
                fdest = new File(numefis);
            System.out.println("*****"+fdest);
            String s;
            document = PDDocument.load(fdest);
            document.getClass();
            if( !document.isEncrypted() ){
                PDFTextStripperByArea stripper = new
                        PDFTextStripperByArea();
                stripper.setSortByPosition( true );
                PDFTextStripper Tstripper = new
                        PDFTextStripper();
                st = Tstripper.getText(document);
                //System.out.println("PDF>>>> "+st.substring(0,st.length()>1000?1000:st.length()));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            if(document!=null)
                try {
                    document.close();
                    if(website)
                        fdest.delete();
                } catch (IOException ex) {
                    Logger.getLogger(PDF_Text.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return st;

    }

}
