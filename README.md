Directed Acyclic Word Graph (DAWG)
====================================
The DAWG is basically the dictionary used in WordsWithFriends. It is small and very fast ( worst case O(length of word) ). It is also used in the Words Solvers on iOS and Android clients and in GWF Service (for stuff like Vision and Word O Meters) , for Bots, etc. You can check out the code to create a new DAWG from a list of words from https://github-ca.corp.zynga.com/degupta/Dawg/ and the way it looks up words should be in DawgArray.{c,java} depending on the platform you are on.

If you want to know how the DAWG is used in the solvers have you can check the code at https://github-ca.corp.zynga.com/degupta/WordGeneratorC
