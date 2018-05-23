package  spamfilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;


public class NaiveBayesFilter {
    //declearing variables

    static double spamTotal = 0;

    static double hamTotal = 0;


    static HashMap<String, Integer> mergedList = new HashMap<String, Integer>();

    static HashMap<String, Integer> stopWords = new HashMap<String, Integer>();

    static HashMap<String, Integer> hamWords = new HashMap<String, Integer>();

    static HashMap<String, Integer> spamWords = new HashMap<String, Integer>();

    public static void main(String[] args) throws IOException
    {
        String filePathHam = "test/data/train/ham"; //file path of training ham folder

        String filePathSpam = "test/data/train/spam"; //file path of training spam folder

        String testPath = "test/data/test"; //file path of test folder

        String stopWordsPath = "test/data/stopwords.txt"; //file path of stopwords folder

        stopWords = formStopWordsList(stopWords, stopWordsPath); //converting stopwords file into a list



        hamWords = formDictionary(filePathHam);  //get ham words from file and removing stopwords

        spamWords = formDictionary(filePathSpam); //get spam words from file and removing stopwords


        double hamDocNumber = 0, spamDocNumber = 0;

        double priorHam = 0, priorSpam = 0;
        //listing the files in a folder

        File folder = new File(filePathHam);

        File[] listOfFiles = folder.listFiles();

        hamDocNumber = listOfFiles.length;

        folder = new File(filePathSpam);

        listOfFiles = folder.listFiles();

        spamDocNumber = listOfFiles.length;
        System.out.println("Training in progress... ");
        
        try{
        TimeUnit.SECONDS.sleep(10);
        }catch(Exception e){}
        //computing priors
        priorHam = hamDocNumber/(hamDocNumber + spamDocNumber);

        priorSpam = spamDocNumber/(hamDocNumber + spamDocNumber);

        //stores conditional probabilitity of spam and ham

        HashMap<String, Double> conditionalHam = new HashMap<String, Double>();
        HashMap<String, Double> conditionalSpam = new HashMap<String, Double>();


        hamTotal= findTotalWords(filePathHam); //removes stopwords and get total words in ham folder
        spamTotal= findTotalWords(filePathSpam); //removes stopwords and get total words in spam folder

        //puts all spam and ham words in a list
        mergedList.putAll(hamWords);
        mergedList.putAll(spamWords);

        //calculates the conditional probability of each word in the spam and ham

        conditionalHam = findConditionalProbability(hamWords, mergedList, hamTotal);
        conditionalSpam = findConditionalProbability(spamWords, mergedList, spamTotal);


        System.out.println("Training complete!!!!");
        
        System.out.println("\nAfter Removal of Stop Words: ");
        System.out.println("--------------------------------------------");
        System.out.println("In the testing folder: ");
        //print result
       performTesting(conditionalHam, conditionalSpam, priorHam, priorSpam, true, testPath, hamTotal, spamTotal, mergedList);


    }

    public static HashMap<String, Double> findConditionalProbability(HashMap<String, Integer> list, HashMap<String, Integer> mergedList, double wordCount)
    {
        HashMap<String, Double> conditional = new HashMap<String, Double>();

        Iterator iterate = list.keySet().iterator();

        while(iterate.hasNext())
        {
            String key = iterate.next().toString();
            double numerator = (double) list.get(key)  + 1;

            double denominator = wordCount + mergedList.size();
            conditional.put(key, numerator/denominator);
        }

        return conditional;
    }

    public static void performTesting(HashMap<String, Double> conditionalHam, HashMap<String, Double> conditionalSpam, double priorHam, double priorSpam, boolean toTest, String testPath, double totalWordsInHam, double totalWordsInSpam, HashMap<String, Integer> mergeList) throws IOException
    {
        String currLine = "";
        int hamClass = 0, spamClass = 0;

        File folder = new File(testPath);
        File[] listOfFiles = folder.listFiles();

        for(int i = 0; i< listOfFiles.length; i++)
        {
            ArrayList<String> listOfWords = new ArrayList<String>();
            File file = listOfFiles[i];

            if(file.isFile() && file.getName().contains("txt"))
            {
                BufferedReader br = new BufferedReader(new FileReader(testPath + "/" + file.getName()));

                while((currLine = br.readLine()) != null)
                {
                    String[] lineArray = currLine.split(" ");

                    for(int j = 0; j< lineArray.length; j++)
                    {
                        if(lineArray[j].length() <= 1)
                        {

                        }

                        if(stopWords.containsKey(lineArray[j]))
                        {

                        }
                        else
                        {
                            listOfWords.add(lineArray[j]);
                        }
                    }
                }
            }

            if(calculateProbability(listOfWords, conditionalHam, conditionalSpam, priorHam, priorSpam, totalWordsInHam, totalWordsInSpam, mergeList) == 1) {

                System.out.println(file.getName() + " is ham");
                hamClass++;
            }

            else {
                System.out.println(file.getName() + " is spam");

                spamClass++;
            }
        }

        System.out.println("No. of ham mails = " + hamClass + "\n" + "No. of spam mails = " + spamClass);


    }

    public static int calculateProbability(ArrayList<String> listOfWords, HashMap<String, Double> conditionalHam, HashMap<String, Double> conditionalSpam, double priorHam, double priorSpam, double totalWordsInHam, double totalWordsInSpam, HashMap<String, Integer> mergeList)
    {
        double valHam = 0;

        for(int i = 0; i< listOfWords.size(); i++)
        {
            if(conditionalHam.get(listOfWords.get(i)) != null)
                valHam = valHam + Math.log(conditionalHam.get(listOfWords.get(i)));

            else
                valHam = valHam + Math.log(1/(totalWordsInHam + mergeList.size()));
        }
        valHam = valHam + Math.log(priorHam);

        double valSpam = 0;

        for(int i = 0; i< listOfWords.size(); i++)
        {
            if(conditionalSpam.get(listOfWords.get(i)) != null)
                valSpam = valSpam + Math.log(conditionalSpam.get(listOfWords.get(i)));

            else
            {
                valSpam = valSpam + Math.log(1/(totalWordsInSpam + mergeList.size()));
            }
        }

        valSpam = valSpam + Math.log(priorSpam);

        if((valHam) > (valSpam))
            return 1;

        else
            return -1;

    }

    public static double findTotalWords(String filepath) throws IOException
    {
        String currLine;
        double wordCount = 0;

        File folder = new File(filepath);
        File[] listOfFiles = folder.listFiles();

        for(int i = 0; i< listOfFiles.length; i++)
        {
            File file = listOfFiles[i];
            if(file.isFile() && file.getName().contains("txt"))
            {
                BufferedReader br = new BufferedReader(new FileReader(filepath + "/" + file.getName()));

                while((currLine = br.readLine()) != null)
                {
                    String[] lineArray = currLine.split(" ");

                    for(int j = 0; j< lineArray.length; j++)
                    {
                        if(lineArray[j].length() <= 1)
                        {

                        }

                        if(stopWords.containsKey(lineArray[j]))
                        {

                        }
                        else
                        {
                            wordCount++;
                        }

                    }
                }
            }
        }
        return wordCount;
    }

    public static HashMap<String, Integer> formDictionary(String filepath) throws IOException
    {

        String currLine;
        HashMap<String, Integer> hamWords = new HashMap<String, Integer>();

        File folder = new File(filepath);
        File[] listOfFiles = folder.listFiles();

        for(int i = 0; i< listOfFiles.length; i++)
        {
            File file = listOfFiles[i];
            if(file.isFile() && file.getName().contains("txt"))
            {
                BufferedReader br = new BufferedReader(new FileReader(filepath + "/" + file.getName()));

                while((currLine = br.readLine()) != null)
                {
                    String[] lineArray = currLine.split(" ");

                    for(int j = 0; j< lineArray.length; j++)
                    {
                        if(lineArray[j].length() <= 1)
                        {

                        }
                        if(stopWords.containsKey(lineArray[j]))
                        {

                        }
                        else
                        {
                            if(hamWords.containsKey(lineArray[j]))
                                hamWords.put(lineArray[j], hamWords.get(lineArray[j]) + 1);

                            else
                                hamWords.put(lineArray[j], 1);
                        }
                    }
                }
            }
        }

        return hamWords;
    }

    public static HashMap<String, Integer> formStopWordsList(HashMap<String, Integer> stopWords, String path) throws FileNotFoundException
    {
        File file = new File(path);
        Scanner scanFile = new Scanner(new FileReader(file));

        while(scanFile.hasNext())
        {
            stopWords.put(scanFile.next(), 1);
        }

        scanFile.close();

        return stopWords;
    }
}