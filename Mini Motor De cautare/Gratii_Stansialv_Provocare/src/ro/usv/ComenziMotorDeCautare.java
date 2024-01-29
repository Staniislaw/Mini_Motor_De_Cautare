package ro.usv;

import com.spire.presentation.IAutoShape;
import com.spire.presentation.ISlide;
import com.spire.presentation.ParagraphEx;
import com.spire.presentation.Presentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class ComenziMotorDeCautare implements IMiniMotorDeCautare {
    private static ComenziMotorDeCautare instance;
    public static ComenziMotorDeCautare getInstance() {
        if(instance==null)
        {
            instance=new ComenziMotorDeCautare();
        }
        return instance;
    }
    private int nrDoc=0;
    private int nrCuvinteCheie=0;
    private Set<String> cuvinteCheie ;
    private Map<String, Set<String>> index;
    Map<String, Boolean> cuvinteStop;
    private boolean useShortDocName = false;
    private Map<String,Boolean>MapDeletedDocument;
    private ComenziMotorDeCautare()
    {
        cuvinteStop= new HashMap<>();
        cuvinteCheie=new HashSet<>();
        index=new HashMap<>();
        MapDeletedDocument=new HashMap<>();
    }

    @Override
    public Pereche<Integer, Integer> build(String numeFisSetSimplificat) {
        try {
            Scanner scanner = new Scanner(new File(numeFisSetSimplificat));
            int nrDoc = 0;

            while (scanner.hasNextLine()) {
                String caleFisier = scanner.nextLine();
                if (scanner.hasNextLine()) {
                    String continutFisier = scanner.nextLine();
                    if (!continutFisier.isEmpty()) {
                        nrDoc++;
                        extrageCuvinteCheie(caleFisier, continutFisier);
                    } else {
                        System.out.println("Lipsa continut pentru documentul " + caleFisier);
                    }
                } else {
                    System.out.println("Lipsa continut pentru documentul " + caleFisier);
                }
            }
            this.nrDoc = nrDoc;
            this.nrCuvinteCheie=cuvinteCheie.size();
            return new Pereche<>(nrDoc, nrCuvinteCheie);
        } catch (FileNotFoundException e) {
            System.err.println("Fisierul nu a fost gasit: " + numeFisSetSimplificat);
            return new Pereche<>(0, 0);
        }
    }



    private boolean isSupportedFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".txt") || fileName.endsWith(".cpp") || fileName.endsWith(".java")
                || fileName.endsWith(".c") || fileName.endsWith(".xml") || fileName.endsWith(".csv")
                || fileName.endsWith(".html") || fileName.endsWith(".css") || fileName.endsWith(".js")
                || fileName.endsWith(".py");
    }

    private void extrageCuvinteCheie(String caleFisier, String continutFisier) {
        if (continutFisier.isEmpty()) {
            return;
        }
        AnalizaText analizaText = new AnalizaText(continutFisier,true);
        analizaText.parcurgeText();

        Set<String> cuvinteCheieAnalizaText = analizaText.getCuvDist();
        MapDeletedDocument.put(caleFisier,true);
        for (String cuvant : cuvinteCheieAnalizaText) {
            if (!cuvinteStop.containsKey(cuvant) ) {
                index.computeIfAbsent(cuvant, k -> new HashSet<>()).add(String.valueOf(caleFisier));
                cuvinteCheie.add(cuvant);
            }
        }
    }

    @Override
    public int getNumberOfKeywords() {
        return nrCuvinteCheie;
    }

    @Override
    public int getNumberOfDocuments() {
        return nrDoc;
    }

    @Override
    public int getNumberOfKeywords(String document) {
        int count = 0;
        //parcurgem toate key intrate si verific daca este acel document acolo daca este il incrementez apoi la final il returnez.
        for (Map.Entry<String, Set<String>> entry : index.entrySet()) {

            if (entry.getValue().contains(document)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public SortedSet<String> getKeywordsOfDocument(String document) {
        SortedSet<String> keywords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (Map.Entry<String, Set<String>> entry : index.entrySet()) {
            if (entry.getValue().contains(document) && MapDeletedDocument.get(document)) {
                keywords.add(entry.getKey());
            }
        }

        return keywords;
    }



    @Override
    public int getNumberOfDocuments(String cuvant_Cheie) {
        cuvant_Cheie=cuvant_Cheie.toLowerCase();
        if (index.containsKey(cuvant_Cheie)) {
            Set<String> documents = index.get(cuvant_Cheie);
            int count = 0;
            for (String document : documents) {
                if(MapDeletedDocument.containsKey(document) && MapDeletedDocument.get(document)) {
                    count++;
                }
            }
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public SortedSet<String> getDocumentsOfKeyword(String cuvant_Cheie) {
        cuvant_Cheie = cuvant_Cheie.toLowerCase();
        SortedSet<String> documents = new TreeSet<>();

        if (index.containsKey(cuvant_Cheie)) {
            for (String document : index.get(cuvant_Cheie)) {
                if(useShortDocName)
                {
                    documents.add(getDocNameForPrint(document));
                }else if
                 (MapDeletedDocument.get(document)) {
                    documents.add(document);
                }

            }
            return documents;
        }
        return null;
    }


    @Override
    public void printDetails() {
        System.out.println("Numar documente: " + getNumberOfDocuments());

        int documentIndex = 1;
        // Documente unice să nu pot afișa același document de 2 or
        Set<String> uniqueDocuments = new HashSet<>();
        List<String> sortedDocuments = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : index.entrySet()) {
            Set<String> documentPaths = entry.getValue();

            for (String document : documentPaths) {
                if(MapDeletedDocument.get(document)) {//aiciam facut modificare pentru a nu adauga in afisare documentele care sunt inactive
                    if (uniqueDocuments.add(document)) {
                        sortedDocuments.add(document);
                    }
                }
            }
        }

        Collections.sort(sortedDocuments);

        for (String document : sortedDocuments) {
            Boolean value = MapDeletedDocument.get(document);
            if (value != null && value) {
                System.out.println(documentIndex + ". " + getDocNameForPrint(document));
                documentIndex++;
            }
        }


        System.out.println("Numar cuvinte cheie: " + getNumberOfKeywords());

        int cuvantIndex = 1;
        List<String> sortedKeywords = new ArrayList<>(index.keySet());
        Collections.sort(sortedKeywords);
        outerLoop:
        for (String cuvantCheie : sortedKeywords) {
            Set<String> documentPaths = index.get(cuvantCheie);
            for (String st : documentPaths) {
                if (!MapDeletedDocument.get(st)) {
                    // sărim la începutul buclei exterioare
                    continue outerLoop;
                }
            }
            System.out.print(cuvantIndex + ". " + cuvantCheie + " - " + documentPaths.size() + " doc., active: [");

            List<String> sortedDocumentPaths = new ArrayList<>(documentPaths);
            Collections.sort(sortedDocumentPaths);
            if(useShortDocName==false) {
                for (int i = 0; i < sortedDocumentPaths.size(); i++) {
                    System.out.print(sortedDocumentPaths.get(i));
                    if (i < sortedDocumentPaths.size() - 1) {
                        System.out.print(", ");
                    }
                }
            }
            else {
                for (int i = 0; i < sortedDocumentPaths.size(); i++) {
                    System.out.print(getDocNameForPrint(sortedDocumentPaths.get(i)));
                    if (i < sortedDocumentPaths.size() - 1) {
                        System.out.print(", ");
                    }
                }
            }




            System.out.println("]"); // Treci la următoarea linie pentru următorul cuvânt cheie
            cuvantIndex++;
        }
    }
    private String getDocNameForPrint(String document) {
        if (useShortDocName) {
            return new File(document).getName();
        } else {
            return document;
        }
    }
    private SortedSet<String> documents = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    @Override
    public SortedSet<String> search(String interogare) {
        interogare = interogare.toLowerCase();

        String[] cuvinte = interogare.split(" ");
        documents = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        // Preiau toate documentele
        documents.addAll(index.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));

        if (cuvinte.length == 2) {
            return getDocumentsOfKeyword(cuvinte[1]);
        }

        Set<String> result = new HashSet<>(documents);
        for (int i = 1; i < cuvinte.length; i++) {
            String cuvant = cuvinte[i];
            if (cuvant.startsWith("+")) {
                Set<String> documentSet = getDocumentsOfKeyword(cuvant.substring(1));
                if (documentSet != null && !documentSet.isEmpty()) {
                    result.addAll(documentSet);
                }
            }
            else if (cuvant.startsWith("-")) {
                Set<String> documentSet = getDocumentsOfKeyword(cuvant.substring(1));
                if (documentSet != null && !documentSet.isEmpty()) {
                    result.removeAll(documentSet);
                }
            }
            else {
                Set<String> documentSet = getDocumentsOfKeyword(cuvant);
                if (documentSet != null && !documentSet.isEmpty()) {
                    result.retainAll(documentSet);
                }
            }
        }

        return new TreeSet<>(result);
    }

    @Override
    public Pereche<Integer, Integer> build(String path, String fisierStopWords) throws Exception {
        cuvinteStop.clear();
        if (fisierStopWords != null) {
            try {
                Scanner scannerStopWords = new Scanner(new File(fisierStopWords));
                while (scannerStopWords.hasNextLine()) {
                    String linieStopWords = scannerStopWords.nextLine();
                    String[] partsStopWords = linieStopWords.split(" ");
                    cuvinteStop.put(partsStopWords[0], true);
                }
            } catch (FileNotFoundException e) {
                System.out.println(e);
            }
        }

        File fileOrDirectory = new File(path);

        if (fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                processFilesInDirectory(fileOrDirectory);
            } else {
                nrDoc++;


                if (isPDFFile(fileOrDirectory)) {
                    String textPDF = PDF_Text.extrage(fileOrDirectory.getPath());
                    MapDeletedDocument.put(fileOrDirectory.toString(), true);
                    AnalizaText analizaText = new AnalizaText(textPDF, false);
                    analizaText.parcurgeText();
                    Set<String> cuvinteCheieAnalizaText = analizaText.getCuvDist();

                    for (String cuvant : cuvinteCheieAnalizaText) {
                        if (!cuvinteStop.containsKey(cuvant)) {
                            index.computeIfAbsent(cuvant, k -> new HashSet<>()).add(String.valueOf(fileOrDirectory.toPath()));
                            cuvinteCheie.add(cuvant);
                        }
                    }
                } else if (isPPTXFile(fileOrDirectory)) {
                    Set<String> cuvinteCheiePPTX = extrageCuvintePPTX(fileOrDirectory.getPath());
                    for (String cuvant : cuvinteCheiePPTX) {
                        if (!cuvinteStop.containsKey(cuvant)) {
                            cuvant=cuvant.trim();
                            index.computeIfAbsent(cuvant, k -> new HashSet<>()).add(String.valueOf(fileOrDirectory.toPath()));
                            cuvinteCheie.add(cuvant);
                        }
                    }
                }
                else
                {
                    extrageCuvinteCheie(fileOrDirectory.toPath());
                }
            }
        } else {
            System.out.println("File or directory does not exist: " + path);
        }

        nrCuvinteCheie = cuvinteCheie.size();
        return new Pereche<>(nrDoc, nrCuvinteCheie);
    }
    private boolean isPPTXFile(File file)
    {
        return file.getName().toLowerCase().endsWith(".pptx");
    }
    private boolean isPDFFile(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }
    private Set<String> extrageCuvintePPTX(String filePath) throws Exception {
        Presentation presentation = new Presentation();
        presentation.loadFromFile(filePath);
        Set<String> cuvinte = new HashSet<>();
        MapDeletedDocument.put(filePath,true);
        for (Object slide : presentation.getSlides()) {
            for (Object shape : ((ISlide) slide).getShapes()) {
                if (shape instanceof IAutoShape) {
                    for (Object tp : ((IAutoShape) shape).getTextFrame().getParagraphs()) {
                        cuvinte.addAll(Arrays.asList(((ParagraphEx) tp).getText().split("\\s+")));
                    }
                }
            }
        }
        return cuvinte;
    }

    private void processFilesInDirectory(File directory) {
        try {
            Files.walk(directory.toPath())
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFile)
                    .forEach(fisier -> {
                        nrDoc++;
                        extrageCuvinteCheie(fisier);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extrageCuvinteCheie(Path caleFisier) {
        try {
            MapDeletedDocument.put(caleFisier.toString(),true);
            String continutFisier = new String(Files.readAllBytes(caleFisier), StandardCharsets.UTF_8);

            AnalizaText analizaText = new AnalizaText(continutFisier,true);
            analizaText.parcurgeText();

            Set<String> cuvinteCheieAnalizaText = analizaText.getCuvDist();

            for (String cuvant : cuvinteCheieAnalizaText) {
                if (!cuvinteStop.containsKey(cuvant) ) {
                        index.computeIfAbsent(cuvant, k -> new HashSet<>()).add(String.valueOf(caleFisier));
                        cuvinteCheie.add(cuvant);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //build C:\Z\asd\ssf.txt C:\D\stopwords-ro.txt
    //printDetails
    //build set.txt C:\D\stopwords-ro.txt
    //build C:\Z\asd\set.txt C:\D\stopwords-ro.txt
    //build C:\Z\asd\testPdf.pdf C:\D\stopwords-ro.txt
    //build C:\Z\asd\manual.pdf C:\D\stopwords-ro.txt
    //build C:\Z\asd\sample1.pptx C:\D\stopwords-ro.txt
    //build C:\Z\asd\Sample.pptx C:\D\stopwords-ro.txt
    //getKeywordsOfDocument C:\Z\asd\ssf.txt true

    @Override
    public Pereche<Integer, Integer> build(String dir, String fisierStopWords, String[] alteExtensii) {
        return null;
    }

    @Override
    public boolean addDocument(String numeFisierDocument) throws Exception {
        Pereche<Integer, Integer> numar= build(numeFisierDocument,null);
        System.out.println("true");
        return  true;
    }
    //addDocument C:\Z\asd\ssf.txt`
    //deleteDocument C:\Z\asd\ssf.txt
    //getKeywordsOfDocument C:\Z\asd\ssf.txt
    //getDocumentsOfKeyword asd
    //search asd
    //undeleteDocument C:\Z\asd\ssf.txt
    @Override
    public boolean addDocument(String numeDoc, String continut, Set<String> stopWords) {

        return false;
    }

    @Override
    public boolean deleteDocument(String numeDocument) {
        if (MapDeletedDocument.containsKey(numeDocument) && MapDeletedDocument.get(numeDocument)) {
            nrDoc--;
            MapDeletedDocument.put(numeDocument, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean undeleteDocument(String numeDocument) {
        if (MapDeletedDocument.containsKey(numeDocument) && !MapDeletedDocument.get(numeDocument)) {
            nrDoc++;
            MapDeletedDocument.put(numeDocument, true);
            return true;
        }
        return false;
    }


    @Override
    public boolean renameDocument(String numeDocument1, String numeDocument2) {
        if (MapDeletedDocument.containsKey(numeDocument1) && MapDeletedDocument.get(numeDocument1)) {
            MapDeletedDocument.put(numeDocument2, true);
            MapDeletedDocument.remove(numeDocument1);
            Set<String> documentPaths = index.get(numeDocument1);
            index.remove(numeDocument1);
            index.put(numeDocument2, documentPaths);

                return true;
            }
        return false;
    }


    @Override
    public Pereche<Integer, Integer> clear() {
        cuvinteCheie.clear();
        index.clear();
        cuvinteStop.clear();
        nrDoc = 0;
        nrCuvinteCheie = 0;
        return new Pereche<>(0, 0);
    }

    @Override
    public boolean setShortDocName(String faraCaleCompleta) {
        if ("true".equalsIgnoreCase(faraCaleCompleta)) {
            useShortDocName = true;
        } else {
            useShortDocName = false;
        }
        System.out.println(useShortDocName);
        return useShortDocName;
    }
    public void execComenzi() throws Exception {
        Scanner scmd = new Scanner(System.in);
        String input;

        while (scmd.hasNextLine()) {
            input = scmd.nextLine();
            String[] parts = input.split(" ");

            if (input.equals("stop")) {
                System.out.println(">>> " + input);
                break;
            }

            System.out.println(">>> " + input);
            if (input.contains("build")) {
                if (parts.length >= 2) {
                    if (parts.length == 2) {
                        // Se transmite doar calea către fișier
                        Pereche<Integer, Integer> rezultat = build(parts[1]);
                        System.out.println(rezultat);
                    } else if (parts.length == 3) {
                        // Se transmit calea către fișier și fișierul de stop words
                        Pereche<Integer, Integer> rezultat = build(parts[1], parts[2]);
                        System.out.println(rezultat);
                    } else if (parts.length == 4) {
                        // Se transmit calea către fișier, fișierul de stop words și lista de extensii
                        String[] extensii = parts[3].split(",");
                        Pereche<Integer, Integer> rezultat = build(parts[1], parts[2], extensii);
                        System.out.println(rezultat);
                    } else {
                        System.out.println("Too many arguments for the 'build' command.");
                    }
                } else {
                    System.out.println("Insufficient arguments for the 'build' command.");
                }
        } else if (input.contains("getNumberOfKeywords")) {
                if (parts.length < 2) {
                    System.out.println(getNumberOfKeywords());
                } else {
                    System.out.println(getNumberOfKeywords(parts[1]));
                }
            } else if (input.contains("getNumberOfDocuments")) {
                if (parts.length < 2) {
                    System.out.println(getNumberOfDocuments());
                } else {
                    System.out.println(getNumberOfDocuments(parts[1]));
                }
            } else if (input.contains("getKeywordsOfDocument")) {
                System.out.println(getKeywordsOfDocument(parts[1]));
            } else if (input.contains("getDocumentsOfKeyword")) {
                System.out.println(getDocumentsOfKeyword(parts[1]));
            } else if (input.contains("printDetails")) {
                printDetails();
            } else if (input.contains("search")) {
                if (parts.length < 2) {
                    System.out.println("Lipsa argument");
                } else {
                    System.out.println(search(input));
                }
            } else if (input.contains("addDocument")) {
                if(parts.length<=2)
                {
                    ComenziMotorDeCautare.getInstance().addDocument(parts[1]);
                }
                else
                {
                    System.out.println("ADD");
                }
            } else if (parts[0].equals("deleteDocument")) {
                if (parts.length == 2) {
                    boolean result = ComenziMotorDeCautare.getInstance().deleteDocument(parts[1]);
                    System.out.println(result);
                }
            } else if (parts[0].equals("undeleteDocument")) {
                if (parts.length == 2) {
                    boolean result = ComenziMotorDeCautare.getInstance().undeleteDocument(parts[1]);
                    System.out.println(result);
                }
            }else if(input.contains("clear")){
                Pereche<Integer,Integer>numar= ComenziMotorDeCautare.getInstance().clear();
                System.out.println(numar);
            }else if(input.contains("setShortDocName")){
                ComenziMotorDeCautare.getInstance().setShortDocName(parts[1]);
            }
            else if (input.contains("rename")) {
                if (parts.length == 3) {
                    String primulDoc = parts[1];
                    String alDoileaDoc = parts[2];
                    boolean result = ComenziMotorDeCautare.getInstance().renameDocument(primulDoc, alDoileaDoc);
                    System.out.println(result ? "Documentul a fost redenumit cu succes." : "Documentul nu a putut fi redenumit.");
                } else {
                    System.out.println("Comanda 'rename' trebuie să aibă două argumente.");
                }
            }
            else {
                System.out.println("Comanda necunoscuta: " + input);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ComenziMotorDeCautare.getInstance().execComenzi();
    }

}
