package ro.usv;

import com.spire.presentation.IAutoShape;
import com.spire.presentation.ISlide;
import com.spire.presentation.ParagraphEx;
import com.spire.presentation.Presentation;

import java.text.Normalizer;
import java.util.*;

public class AnalizaText {

    private String text;
    private String[] atomiLexicali;
    private SortedSet<String> cuvDist;
    private SortedSet<String> cuvDistDia;
    private HashMap<String,List<String>> dictAnagrame;
    private SortedMap<Character, Integer> frecvLitere;
    private long nrcuvinte;
    private boolean acceptSiIdentificatori;
    private String regexMatcher;
    private String regexSplit = "[^a-zA-Z0-9_@ăĂâÂîÎşȘţȚ$]+";
    public AnalizaText(String text){
        this(text, false);
    }
    public AnalizaText(String text, boolean acceptSiIdentificatori) {
        this.text = text;
        this.acceptSiIdentificatori = acceptSiIdentificatori;
        regexMatcher = acceptSiIdentificatori ? "[\\p{L}0-9_$@]*" : "\\p{L}+";
        parcurgeText();
    }

    public void parcurgeText() {
        atomiLexicali = text.split(regexSplit);
        nrcuvinte = 0;
        cuvDist = new TreeSet<>();
        cuvDistDia = new TreeSet<>();
        dictAnagrame = new HashMap<>();
        frecvLitere = new TreeMap<>();

        for (String cuv : atomiLexicali) {
            int _1 = 1;
            if (cuv.matches(regexMatcher)) {
                nrTotalLitere += cuv.length();
                nrcuvinte++;
                cuvDist.add(cuv.toLowerCase());  // TreeSet retine doar cheile (cuvintele) distincte
                cuvDistDia.add(cuv.toLowerCase()); // Adăugați cuvântul cu diacritice în TreeSet-ul separat

                if (!acceptSiIdentificatori) {
                    char[] litere = cuv.toCharArray();
                    addAnagrame(cuv, litere);
                    addFrecventaLitere(litere);
                }
            }
        }
    }


    private void addAnagrame(String cuv, char[] litere){
        if(cuv.length()==1)  // || cuv.matches("\\d*"))
            return;
        Arrays.sort(litere);
        String sirLitere = String.valueOf(litere);
        List lcuv = dictAnagrame.get(sirLitere);
        if(lcuv==null) {
            lcuv=new  ArrayList();
        }
        if(!lcuv.contains(cuv)) {
            lcuv.add(cuv);
            dictAnagrame.put(sirLitere, lcuv);
        }
    }

    private long nrTotalLitere=0;

    private void addFrecventaLitere(char[] litere){
        Integer nr;
        for(char car: litere){
            if(! Character.isLetter(car)) continue;
            nr = frecvLitere.get(car);
            if(nr==null){
                frecvLitere.put(car, 1);
            } else {
                frecvLitere.put(car, ++nr);
            }
        }
    }


    public void setText(String text) {
        this.text = text;
        parcurgeText();
    }
    public SortedSet<String> getCuvDist() {
        return cuvDist;
    }
    public SortedSet<String> getCuvDistDia() {
        return cuvDistDia;
    }

    public HashMap<String, List<String>> getDictAnagrame() {
        return dictAnagrame;
    }
    public long getNrcuvinte() {
        return nrcuvinte;
    }

    public long getNrTotalLitere() {
        return nrTotalLitere;
    }

    public String getText() {
        return text;
    }
    public SortedMap<Character, Integer> getFrecvLitere() {
        return frecvLitere;
    }

    public List<Map.Entry<Character, Integer>> getIntrariLitereFrecvDesc(){
        Collection<Map.Entry<Character, Integer>> frecvIntrari =
                (Collection<Map.Entry<Character, Integer>>)frecvLitere.entrySet();
        List<Map.Entry<Character, Integer>> lstIntrari= new ArrayList<>(frecvIntrari);
        Collections.sort(lstIntrari,
                Comparator.comparing(x->((Map.Entry<Character, Integer>)x).getValue()).reversed());

        return lstIntrari;
    }

    public String[] getAtomiLexicali() {
        return atomiLexicali;
    }

    public void afisInfo(boolean afisTextInitial, boolean afisCuvinteDistincte, boolean afisListanagrame, boolean afisFrecvLitere){
        if(afisTextInitial)
            System.out.println("Textul initial:\n"+getText());

        if(afisCuvinteDistincte) {
            System.out.println("Cuvinte distincte "+ getCuvDist());
            System.out.println("Total cuvinte distincte:"+getCuvDist().size()+" / "+getNrcuvinte());
        }
        if(afisListanagrame) {
            int ncuvAnagrame=0;
            for(String ls:dictAnagrame.keySet()) {
                List lstAnagrame = dictAnagrame.get(ls);
                if (lstAnagrame.size() > 1){
                    ncuvAnagrame++;
                    System.out.println(ls + "->" + lstAnagrame);
                }
            }
            System.out.println("Total "+ncuvAnagrame+" grupuri de anagrame");
        }

        if(afisFrecvLitere){
            SortedMap<Character, Integer> frecv = getFrecvLitere();
            System.out.println("Nr.litere distincte="+frecv.size()+" / Nr.total litere="+nrTotalLitere);
            System.out.println("Freventa litere:");
            for(Map.Entry e: frecv.entrySet())
                System.out.printf("%c = %.2f%%  ", (char)e.getKey(),
                        ((Integer)e.getValue()).floatValue()/nrTotalLitere*100.);

            System.out.println("\nIn ordinea descrecatoare a frecventelor:");
            StringBuilder sbfrecv=new StringBuilder();
//          for(Map.Entry e: getFrecvLitereDescrescator().entrySet()) { //1. LinkedHashMap sortat
            for(Map.Entry e: getIntrariLitereFrecvDesc()){     // solutia 2.List<Map.Entry>  sortata
                System.out.printf("%c = %.2f%%  ", (char) e.getKey(),
                        ((Integer) e.getValue()).floatValue() / nrTotalLitere * 100.);
                sbfrecv.append(e.getKey());
            }
            System.out.println("\n"+sbfrecv.toString().toUpperCase());
        }
    }



    public static void main(String[] args) throws Exception {
        Presentation presentation = new Presentation();
        presentation.loadFromFile("C:\\Z\\asd\\samplepptx.pptx");
        StringBuilder buffer = new StringBuilder();

        for (Object slide : presentation.getSlides()) {
            for (Object shape : ((ISlide) slide).getShapes()) {
                if (shape instanceof IAutoShape) {
                    for (Object tp : ((IAutoShape) shape).getTextFrame().getParagraphs()) {
                        buffer.append(((ParagraphEx) tp).getText() + "\n");
                    }
                }
            }
        }

            System.out.println(buffer);
    }
}
