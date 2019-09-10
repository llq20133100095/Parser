import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;

import java.io.*;
import java.util.*;

public class stanfordParser {

    public static void main(String[] args) throws IOException{
        //Init Stanford parser
        String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        if (args.length > 0) {
            parserModel = args[0];
        }
        LexicalizedParser lp = LexicalizedParser.loadModel(parserModel, "-outputFormatOptions", "basicDependencies");

        FileReader dataFile=new FileReader("./data/train_sen.txt");
        FileWriter dataSegment=new FileWriter("./data/train_segment.txt");
        FileWriter dataEntityPos=new FileWriter("./data/train_entity_position.txt");
        FileWriter dataWrite=new FileWriter("./data/train_dependencies.txt");
        FileWriter dataWriteTag=new FileWriter("./data/train_tagged.txt");

        BufferedReader bfRead=new BufferedReader(dataFile);

        //read data in each lines
        String line=null;
        int lineNum=1;
        while((line=bfRead.readLine())!=null){
//            line=line.split("\t")[1];
            dataSegment.write(lineNum+"\t");
            dataEntityPos.write(lineNum+" ");

            //denpendencies parser
//            demoAPI(lp, line, dataSegment, dataEntityPos, dataWrite, dataWriteTag,lineNum);
            demoAPISemveal(lp, line, dataSegment, dataEntityPos, dataWrite, dataWriteTag,lineNum);
//            demoAPIKbp(lp, line, dataSegment, dataEntityPos, dataWrite, dataWriteTag,lineNum);

            lineNum++;
//            if(lineNum==27){
//                break;
//            }
        }
        bfRead.close();
        dataSegment.close();
        dataEntityPos.close();
        dataWrite.close();
        dataWriteTag.close();
    }

    /**
     * demoAPI demonstrates other ways of calling the parser with
     * already tokenized text, or in some cases, raw text that needs to
     * be tokenized as a single sentence.  Output is handled with a
     * TreePrint object.  Note that the options used when creating the
     * TreePrint can determine what results to print out.  Once again,
     * one can capture the output by passing a PrintWriter to
     * TreePrint.printTree. This code is for English.
     */
    public static void demoAPI(LexicalizedParser lp,String sent,FileWriter dataSegment,FileWriter dataEntityPos,FileWriter dataWrite,FileWriter dataWriteTag,int lineNum) throws IOException{
        // This option shows loading and using an explicit tokenizer
//        String sent1="Bills on ports and immigration were submitted by Senator Brownback, Republican of Kansas";
        TokenizerFactory<CoreLabel> tokenizerFactory =
                PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        Tokenizer<CoreLabel> tok =
                tokenizerFactory.getTokenizer(new StringReader(sent));
        List<CoreLabel> rawWords2 = tok.tokenize();

        //Write sentence segment
        Iterator<CoreLabel> iter=rawWords2.iterator();
        while(iter.hasNext()){
            dataSegment.write(iter.next().toString() + " ");
        }
        dataSegment.write("\r\n");


        //Remove "<e1>" and "</e1>" and "<e2>" and "</e2>".
        int e1Pos=-1;
        int e1PosGan=-1;
        int e2Pos=-1;
        int e2PosGan=-1;

        for(int i=0;i<rawWords2.size();i++){
            if(rawWords2.get(i).toString().equals("<e1>")){
                e1Pos=i;
            }
            if(rawWords2.get(i).toString().equals("</e1>")){
                e1PosGan=i;
            }
            if(rawWords2.get(i).toString().equals("<e2>")){
                e2Pos=i;
            }
            if(rawWords2.get(i).toString().equals("</e2>")){
                e2PosGan=i;
            }
        }

        if(e1Pos<e2Pos) {
            rawWords2.remove(e1Pos);
            rawWords2.remove(e1PosGan - 1);
            rawWords2.remove(e2Pos - 2);
            try{
                rawWords2.remove(e2PosGan - 3);
            }
            catch (Exception e){
                System.out.println(lineNum);
                e.printStackTrace();
            }

            //Record entity position
            int entityE1=e1PosGan-1;
            int entityE2=e2PosGan-3;
            dataEntityPos.write(entityE1+" <e> "+entityE2);
            dataEntityPos.write("\r\n");
        }else{
            rawWords2.remove(e2Pos);
            rawWords2.remove(e2PosGan - 1);
            rawWords2.remove(e1Pos - 2);
            rawWords2.remove(e1PosGan - 3);

            //Record entity position
            int entityE1=e1PosGan-3;
            int entityE2=e2PosGan-1;
            dataEntityPos.write(entityE1+" <e> "+entityE2);
            dataEntityPos.write("\r\n");
        }

        Tree parse = lp.apply(rawWords2);

        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
        // Uncomment the following line to obtain original Stanford Dependencies
        tlp.setGenerateOriginalDependencies(true);
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependencies();

        try{
            //Write the dependencies
            Iterator<TypedDependency> iterTdl=tdl.iterator();
            while(iterTdl.hasNext()){
                dataWrite.write(iterTdl.next().toString() + "\t");
//                System.out.println(iter.next().toString());
            }
            dataWrite.write("\r\n");


            //Write the tagged word
            ArrayList<TaggedWord> tag=parse.taggedYield();
            Iterator<TaggedWord>  iterTag=tag.iterator();
            while(iterTag.hasNext()){
                dataWriteTag.write(iterTag.next().toString() + "\t");
            }
            dataWriteTag.write("\r\n");

        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    /***
     *
     * @param lp
     * @param sent
     * @param dataSegment
     * @param dataEntityPos
     * @param dataWrite
     * @param dataWriteTag
     * @param lineNum
     * @throws IOException
     */
    public static void demoAPISemveal(LexicalizedParser lp,String sent,FileWriter dataSegment,FileWriter dataEntityPos,FileWriter dataWrite,FileWriter dataWriteTag,int lineNum) throws IOException{
        List<CoreLabel> rawWords2=new ArrayList<>();
        for(String x: sent.split(" ")){
            rawWords2.add(CoreLabel.wordFromString(x));
        }

        //Remove "<e1>" and "</e1>" and "<e2>" and "</e2>".
        int e1Pos=-1;
        int e1PosGan=-1;
        int e2Pos=-1;
        int e2PosGan=-1;

        for(int i=0;i<rawWords2.size();i++){
            if(rawWords2.get(i).toString().equals("<e1>")){
                e1Pos=i;
            }
            if(rawWords2.get(i).toString().equals("</e1>")){
                e1PosGan=i;
            }
            if(rawWords2.get(i).toString().equals("<e2>")){
                e2Pos=i;
            }
            if(rawWords2.get(i).toString().equals("</e2>")){
                e2PosGan=i;
            }
        }

        if(e1Pos<e2Pos) {
            rawWords2.remove(e1Pos);
            rawWords2.remove(e1PosGan - 1);
            rawWords2.remove(e2Pos - 2);
            try{
                rawWords2.remove(e2PosGan - 3);
            }
            catch (Exception e){
                System.out.println(lineNum);
                e.printStackTrace();
            }

            //Record entity position
            int entityE1=e1PosGan-1;
            int entityE2=e2PosGan-3;
            dataEntityPos.write(entityE1+" <e> "+entityE2);
            dataEntityPos.write("\r\n");
        }else{
            rawWords2.remove(e2Pos);
            rawWords2.remove(e2PosGan - 1);
            rawWords2.remove(e1Pos - 2);
            rawWords2.remove(e1PosGan - 3);

            //Record entity position
            int entityE1=e1PosGan-3;
            int entityE2=e2PosGan-1;
            dataEntityPos.write(entityE1+" <e> "+entityE2);
            dataEntityPos.write("\r\n");
        }

        Tree parse = lp.apply(rawWords2);

        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
        // Uncomment the following line to obtain original Stanford Dependencies
        tlp.setGenerateOriginalDependencies(true);
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependencies();

        try{
            //Write the dependencies
            Iterator<TypedDependency> iterTdl=tdl.iterator();
            while(iterTdl.hasNext()){
                dataWrite.write(iterTdl.next().toString() + "\t");
//                System.out.println(iter.next().toString());
            }
            dataWrite.write("\r\n");


            //Write the tagged word
            ArrayList<TaggedWord> tag=parse.taggedYield();
            Iterator<TaggedWord>  iterTag=tag.iterator();
            while(iterTag.hasNext()){
                dataWriteTag.write(iterTag.next().toString() + "\t");
            }
            dataWriteTag.write("\r\n");

        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    /***
     *
     * @param lp
     * @param sent
     * @param dataSegment
     * @param dataEntityPos
     * @param dataWrite
     * @param dataWriteTag
     * @param lineNum
     * @throws IOException
     */
    public static void demoAPIKbp(LexicalizedParser lp,String sent,FileWriter dataSegment,FileWriter dataEntityPos,FileWriter dataWrite,FileWriter dataWriteTag,int lineNum) throws IOException{
        List<CoreLabel> rawWords2=new ArrayList<>();
        for(String x: sent.split(" ")){
            rawWords2.add(CoreLabel.wordFromString(x));
        }

        //Remove "<e1>" and "</e1>" and "<e2>" and "</e2>".
        int e1Pos=-1;
        int e1PosGan=-1;
        int e2Pos=-1;
        int e2PosGan=-1;

        for(int i=0;i<rawWords2.size();i++){
            if(rawWords2.get(i).toString().equals("<e1>")){
                e1Pos=i;
            }
            if(rawWords2.get(i).toString().equals("</e1>")){
                e1PosGan=i;
            }
            if(rawWords2.get(i).toString().equals("<e2>")){
                e2Pos=i;
            }
            if(rawWords2.get(i).toString().equals("</e2>")){
                e2PosGan=i;
            }
        }

        if(e1Pos<e2Pos) {
            rawWords2.remove(e1Pos);
            rawWords2.remove(e1PosGan - 1);
            rawWords2.remove(e2Pos - 2);
            try{
                rawWords2.remove(e2PosGan - 3);
            }
            catch (Exception e){
                System.out.println(lineNum);
                e.printStackTrace();
            }

            //Record entity position
            int entityE1=e1PosGan-1;
            int entityE2=e2PosGan-3;
            dataEntityPos.write(entityE1+" <e> "+entityE2);
            dataEntityPos.write("\r\n");
        }else{
            rawWords2.remove(e2Pos);
            rawWords2.remove(e2PosGan - 1);
            rawWords2.remove(e1Pos - 2);
            rawWords2.remove(e1PosGan - 3);

            //Record entity position
            int entityE1=e1PosGan-3;
            int entityE2=e2PosGan-1;
            dataEntityPos.write(entityE1+" <e> "+entityE2);
            dataEntityPos.write("\r\n");
        }

        Tree parse = lp.apply(rawWords2);

        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
        // Uncomment the following line to obtain original Stanford Dependencies
        tlp.setGenerateOriginalDependencies(true);
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependencies();

        try{
            //Write the dependencies
            Iterator<TypedDependency> iterTdl=tdl.iterator();
            while(iterTdl.hasNext()){
                dataWrite.write(iterTdl.next().toString() + "\t");
//                System.out.println(iter.next().toString());
            }
            dataWrite.write("\r\n");


            //Write the tagged word
            ArrayList<TaggedWord> tag=parse.taggedYield();
            Iterator<TaggedWord>  iterTag=tag.iterator();
            while(iterTag.hasNext()){
                dataWriteTag.write(iterTag.next().toString() + "\t");
            }
            dataWriteTag.write("\r\n");

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
