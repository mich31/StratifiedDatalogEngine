package fr.univlyon1.mif37.dex;

import fr.univlyon1.mif37.dex.mapping.*;
import fr.univlyon1.mif37.dex.parser.MappingParser;
import fr.univlyon1.mif37.dex.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static boolean positive(Mapping mp)
    {
        for (Tgd t:mp.getTgds())
        {
            for (Literal l:t.getLeft())
            {
                //System.out.println(l.getAtom().getName());
                //System.out.println(l.getFlag()+"\n\n");
                if(!l.getFlag())
                {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean semipositive(Mapping mp)
    {
        for (Tgd t:mp.getTgds())
        {
            for (Literal l:t.getLeft())
            {
                if(!l.getFlag())
                {
                    for (Relation rel:mp.getEDB())
                    {
                        if(rel.getName().equals(l.getAtom().getName()))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean stratified(Mapping mp)
    {
        for (Tgd t:mp.getTgds())
        {
            for (Literal l:t.getLeft())
            {
                if(!l.getFlag())
                {
                    for (AbstractRelation rel:mp.getIDB())
                    {
                        if(rel.getName().equals(l.getAtom().getName()))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static Map<String,Integer> stratification(Mapping mp)
    {
        Map<String,Integer> predicates = new HashMap<>();
        for(Relation rel:mp.getEDB())
        {
            if(!predicates.containsKey(rel.getName()))
            {
                predicates.put(rel.getName(),1);
            }
        }

        for(AbstractRelation rel : mp.getIDB())
        {
            if(!predicates.containsKey(rel.getName()))
            {
                predicates.put(rel.getName(),1);
            }
        }

        for(Tgd tgd:mp.getTgds())
        {
            for(Literal l:tgd.getLeft())
            {
                if(!predicates.containsKey(l.getAtom().getName()))
                {
                    predicates.put(l.getAtom().getName(),1);
                }
            }

            if(!predicates.containsKey(tgd.getRight().getName()))
            {
                predicates.put(tgd.getRight().getName(),1);
            }
        }

        ArrayList<String> head = new ArrayList<>();
        for(Tgd tgd : mp.getTgds())
        {
            if(!head.contains(tgd.getRight().getName()))
            {
                head.add(tgd.getRight().getName());
            }
        }

        for(String name:head)
        {
            for(Tgd tgd : mp.getTgds()) // Negated subgoals
            {
                if(name.equals(tgd.getRight().getName()))
                {
                    for(Literal l:tgd.getLeft())
                    {
                        if(!l.getFlag())
                        {
                            int max = Math.max(predicates.get(tgd.getRight().getName()),predicates.get(l.getAtom().getName()) + 1);
                            predicates.put(tgd.getRight().getName(),max);
                        }
                    }
                }
            }
        }

        for(String name:head)
        {
            for(Tgd tgd : mp.getTgds()) // Nonnegated subgoals
            {
                if(name.equals(tgd.getRight().getName()))
                {
                    for(Literal l:tgd.getLeft())
                    {
                        if(l.getFlag())
                        {
                            int max = Math.max(predicates.get(tgd.getRight().getName()),predicates.get(l.getAtom().getName()));
                            predicates.put(tgd.getRight().getName(),max);
                        }
                    }
                }
            }
        }

        return predicates;
    }

    public static void slicing(Map<String,Integer> map,Mapping mp)
    {
    }

    public static void main(String[] args) throws ParseException {
        System.out.println("Please enter the path of the input text file:");
        // ./src/test/resources/sample-mapping.txt  link(PartDieu,Debourg) reachable(PartDieu,Perrache)
        Scanner sc = new Scanner(System.in);
        String filename = sc.nextLine();
        File file = new File(filename);
        String content = "";

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null)
            {
                content += line+"\n";
            }
            reader.close();
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
        }

        System.out.println(content);

        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        //MappingParser mp = new MappingParser(System.in); // System.in <= Nom du fichier
        MappingParser mp = new MappingParser(stream); // System.in <= Nom du fichier
        Mapping mapping = mp.mapping();

        //positive(mapping);
        System.out.println("Semi-positive: "+semipositive(mapping));
        System.out.println("Stratified: "+stratified(mapping));
        stratification(mapping);
        LOG.info("Parsed {} edb(s), {} idb(s) and {} tgd(s).",
                mapping.getEDB().size(),
                mapping.getIDB().size(),
                mapping.getTgds().size());
    }
}
