package com.degupta.dawg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class WordChecker {

	public static final String WORDS_THAT_SHOULD_EXIST = "airshow,airshows,blah,crafter,crafters,cuvee,cuvees,fests,firewall,firewalls,fleuron,fleurons,frisbee,frisbees,homepage,homepages,intranet,intranets,meh,moai,mohawk,mohawks,morphed,morphing,oakier,oakiest,oaky,ollie,ollies,panner,panners,realtor,realtors,reuptake,sexting,shawarma,shawarmas,spork,sporks,strobed,tofu,wetsuit,wetsuits,shizzle,amex,halloween,halloweens,dracula,goosebump,goosebumps,selfie,selfies,twerk,twerks,twerking,twerked";
	public static final String WORDS_THAT_SHOULDNT_EXIST = "gyp,gyps,jigaboo,jigaboos,kike,kikes,wetback,wetbacks,dago,dagos,dagoes,wop,wops";

	public static void main(String[] args) {
		HashMap<Character, ArrayList<String>> words = TrieNode.getWords(
				"src/words", false);

		HashSet<String> allWords = new HashSet<String>();

		for (Entry<Character, ArrayList<String>> entry : words.entrySet()) {
			allWords.addAll(entry.getValue());
		}

		HashSet<String> whitelist = new HashSet<String>(
				Arrays.asList(WORDS_THAT_SHOULD_EXIST.split(",")));
		HashSet<String> blacklist = new HashSet<String>(
				Arrays.asList(WORDS_THAT_SHOULDNT_EXIST.split(",")));

		boolean whitelistHeader = false;
		for (String word : whitelist) {
			if (!allWords.contains(word)) {
				if (!whitelistHeader) {
					System.out.println("Missing WhiteListed Words:");
					whitelistHeader = true;
				}
				System.out.println(word);
			}
		}

		boolean blacklistHeader = false;
		for (String word : blacklist) {
			if (allWords.contains(word)) {
				if (!blacklistHeader) {
					System.out.println("Included Blacklisted Words:");
					blacklistHeader = true;
				}
				System.out.println(word);
			}
		}

	}
}
