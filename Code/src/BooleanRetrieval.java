import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.io.*;
import java.util.*;

public class BooleanRetrieval {

	HashMap<String, Set<Integer>> invIndex;
	int [][] docs;
	HashSet<String> vocab;
	HashMap<Integer, String> map;  // int -> word
	HashMap<String, Integer> i_map; // inv word -> int map

	public BooleanRetrieval() throws Exception{
		// Initialize variables and Format the data using a pre-processing class and set up variables
		invIndex = new HashMap<String, Set<Integer>>();
		DatasetFormatter formater = new DatasetFormatter();
		formater.textCorpusFormatter("./all.txt");
		docs = formater.getDocs();
		vocab = formater.getVocab();
		map = formater.getVocabMap();
		i_map = formater.getInvMap();
	}

	void createPostingList(){
		//Initialze the inverted index with a SortedSet (so that the later additions become easy!)
		for(String s:vocab){
			invIndex.put(s, new TreeSet<Integer>());
		}
		//for each doc
		for(int i=0; i<docs.length; i++){
			//for each word of that doc
			for(int j=0; j<docs[i].length; j++){
				//Get the actual word in position j of doc i
				String w = map.get(docs[i][j]);

				/* TO-DO:
				Get the existing posting list for this word w and add the new doc in the list. 
				Keep in mind doc indices start from 1, we need to add 1 to the doc index , i
				 */
				Set<Integer> postListing=invIndex.get(w);
				postListing.add(i+1);

			}

		}
	}

	Set<Integer> intersection(Set<Integer> a, Set<Integer> b){
		/*
		First convert the posting lists from sorted set to something we 
		can iterate easily using an index. I choose to use ArrayList<Integer>.
		Once can also use other enumerable.
		 */
		ArrayList<Integer> PostingList_a = new ArrayList<Integer>(a);
		ArrayList<Integer> PostingList_b = new ArrayList<Integer>(b);
		TreeSet result = new TreeSet();

		//Set indices to iterate two lists. I use i, j
		int i = 0;
		int j = 0;

		while(i!=PostingList_a.size() && j!=PostingList_b.size()){
			//TO-DO: Implement the intersection algorithm here
			if(PostingList_a.get(i).equals(PostingList_b.get(j)))
			{
				result.add(PostingList_a.get(i));
				i++;
				j++;
			}
			else if(PostingList_a.get(i) < PostingList_b.get(j))
				i++;
			else
				j++;
		}
		return result;
	}

	Set <Integer> evaluateANDQuery(String a, String b){
		return intersection(invIndex.get(a), invIndex.get(b));
	}

	Set<Integer> union(Set<Integer> a, Set<Integer> b){
		/*
		 * IMP note: you are required to implement OR and cannot use Java Collections methods directly, e.g., .addAll whcih solves union in 1 line!
		 * TO-DO: Figure out how to perform union extending the posting list intersection method discussed in class?
		 */
		TreeSet result = new TreeSet();
		// Implement Union here

		for (Iterator<Integer> aIt=a.iterator(); aIt.hasNext(); ) 
			result.add(aIt.next());

		for (Iterator<Integer> bIt=b.iterator(); bIt.hasNext(); ) 
			result.add(bIt.next());

		return result;
	}

	Set <Integer> evaluateORQuery(String a, String b){
		return union(invIndex.get(a), invIndex.get(b));
	}

	Set<Integer> not(Set<Integer> a){
		TreeSet result = new TreeSet();
		/*
		 Hint:
		 NOT is very simple. I traverse the sorted posting list between i and i+1 index
		 and add the other (NOT) terms in this posting list between these two pointers
		 First convert the posting lists from sorted set to something we 
		 can iterate easily using an index. I choose to use ArrayList<Integer>.
		 Once can also use other enumerable.


		 */

		ArrayList<Integer> PostingList_a = new ArrayList<Integer>(a);
		int total_docs = docs.length;
		// TO-DO: Implement the not method using above idea or anything you find better!
		for(int i=1;i<=total_docs;i++)
			result.add(i);
		for (Integer aVal : PostingList_a) {
			result.remove(aVal);
		}
		return result;
	}

	Set <Integer> evaluateNOTQuery(String a){
		return not(invIndex.get(a));
	}

	Set <Integer> evaluateAND_NOTQuery(String a, String b){
		return intersection(invIndex.get(a), not(invIndex.get(b)));
	}
	public static void main(String[] args) throws Exception {


		//Initialize parameters
		BooleanRetrieval model = new BooleanRetrieval();

		//Generate posting lists
		model.createPostingList();

		//		 query_type query_string output_file_path
		String output_file_path="";
		String query_type = args[0];
		String output="";
		if(query_type.equals("PLIST")) {
			if(args.length!=3) {
				System.out.println("Invalid number of arguments");
			}else {
				output_file_path=args[2];
				output=args[1].toLowerCase() + " -> " + model.invIndex.get(args[1].toLowerCase());
			}
		}else if (query_type.equals("AND") || query_type.equals("OR")) {
			if(args.length!=5) {
				System.out.println("Invalid number of arguments");
			}else {
				output_file_path=args[4];
				if(query_type.equals("AND")) {
					output=args[1].toLowerCase()+" "+args[2]+" "+args[3].toLowerCase()+" -> ";
					output+=model.evaluateANDQuery(args[1].toLowerCase(), args[3].toLowerCase()).toString();
				}else {
					output=args[1].toLowerCase()+" "+args[2]+" "+args[3].toLowerCase()+" -> ";
					output+=model.evaluateORQuery(args[1].toLowerCase(), args[3].toLowerCase()).toString();
				}
			}
		}else if(query_type.equals("AND-NOT")) {
			if(args.length!=6) {
				System.out.println("Invalid number of arguments");
			}else {
				output_file_path=args[5];
				String arg4=args[4].split("\\)")[0];
				output=args[1].toLowerCase()+" "+args[2]+" "+args[3]+" "+args[4]+" -> ";
				output+=model.evaluateAND_NOTQuery(args[1].toLowerCase(), arg4.toLowerCase()).toString();
			}
		}else {
			System.out.println("Invalid query_type");
		}
		FileWriter fileWriter = new FileWriter(new File(output_file_path));

		fileWriter.write(output);
		fileWriter.flush();

	}

}