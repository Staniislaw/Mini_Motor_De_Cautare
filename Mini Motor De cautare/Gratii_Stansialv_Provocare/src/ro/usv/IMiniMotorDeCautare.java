package ro.usv;

import java.util.Set;
import java.util.SortedSet;
public interface IMiniMotorDeCautare {
    public Pereche<Integer,Integer> build(String numeFisSetSimplificat);
    // indexeaza setul simplificat din fisier (adauga în motor)
// returneaza (nr_doc, nr_cuvinte_cheie) ale motorulului dupa build
    public int getNumberOfKeywords(); // returneaza nr. total de cuvinte cheie
// din motor

    public int getNumberOfDocuments(); //return nr. total de documente din motor
    public int getNumberOfKeywords(String document);
    // returneaza nr. de cuvinte cheie ale documentului
    public SortedSet<String> getKeywordsOfDocument(String document);
    // returneaza lista de cuvinte cheie ale documentului
    public int getNumberOfDocuments(String cuvant_Cheie);
    // returneaza nr. total de documente associate la cuvânt Cheie
    public SortedSet<String> getDocumentsOfKeyword(String cuvant_Cheie);
    // returneaza lista de documente care contin cuvânt ul cheie
    public void printDetails(); // afiseaza nr. de cuvinte cheie, de documente,
    // apoi pentru fiecare cuvânt cheie afiseaza
// "cuvânt ul cheie -> nr.de documente referite și lista documentelor"
    public SortedSet<String> search(String interogare);
    // în prima etapa (conditia minima pt. nota 5) se considera
// ca interogare este un singur cuvânt și metoda returneaza
// multimea returnata de getDocumentsOfKeyword(interogare)
// --------------------------------------------------------------------------------
// ---- toate metodele prezentate pana aici fac parte conditia minima pt. nota 5 --
// --------------------------------------------------------------------------------
// în a doua etapa, interogare poate fi compusa din mai mule cuvinte cheie
// fiecare putand fi prefixate de -(negatie) sau +(sau)
    public Pereche<Integer,Integer> build(String dir, String fisierStopWords) throws Exception;
    // indexeaza toate doc text din director, recursiv, daca dir contine ale
// directoare cauta și în ele documente text (*.txt, *.java), insa din
// cuvintele gasite în doc. exclude cuvintetle din fisierul stopWords
// returneaza (nr_doc, nr_cuvinte_cheie) ale motorulului dupa build
    public Pereche<Integer,Integer> build(String dir, String fisierStopWords,
                                          String[] alteExtensii);
    // indexeaza recursiv (adauga în motor) documentele din director, de tip
// text + cele cu alteExtensii, nu ia în considerare cuvintele stopWords
// returneaza (nr_doc, nr_cuvinte_cheie) ale motorulului dupa build
    public boolean addDocument(String numeFisierDocument) throws Exception;
    // se adauga în motor doc. (fisierul numeFisierDocument
// returneaza true daca operatia a reusit
    public boolean addDocument(String numeDoc, String continut,Set<String> stopWords);
    // se adauga documentul și din cuv. cheie se extrag stopWords
// returneaza true daca operatia a reusit
    public boolean deleteDocument(String numeDocument); // se elimina din motor
    // documentul; returneaza true daca operatia a reusit
    public boolean undeleteDocument(String numeDocument); // se readuce în moor
    // documentul; returneaza true daca operati a reusit
    public boolean renameDocument(String numeDocument1, String numeDocument2);
    //se redenumeste documentul în index
// documentul; returneaza true daca operati a reusit
    public Pereche<Integer, Integer> clear(); // clear motor
    // se elimina din motor toate cuvintele cheie și toate documentele
// returneaza (nr_doc, nr_cuvinte_cheie), in mod normal (0, 0)
    public boolean setShortDocName(String faraCaleCompleta); //seteaza afisarea
// numelui documentelor: "true"- se afiseaza fara PATH
// "false" ori altceva – se afiseaza cu calea completa
// returneaza valoarea boolean indicata de argument
}