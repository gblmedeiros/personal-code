package algorithms.common;

public class Palindrome {

	/**
	 * Verifies if a word is a palindrome. 
	 * Given a word, takes half word and compare each char until it finds a mismatch.  
	 * If @param caseSensitive true, will transform to lower case during comparison
	 * Also can accept spaces if @param ignoreSpaces true. In this case, when match a space, it will just consume
	 * until it finds a non space char.  
	 * @param word
	 * @param caseSensitive
	 * @param ignoreSpaces
	 */
	public static void isPalindrome(String word, boolean caseSensitive, boolean ignoreSpaces) {
		char[] charArray = word.toCharArray();
		boolean pal = true; 
		int i = 0;
		int j = charArray.length - 1; 
		
		while (i < j && pal == true) {
			char ini = charArray[i];
			char end = charArray[j];
			if (!caseSensitive) {
				ini = Character.toLowerCase(ini);
				end = Character.toLowerCase(end);
			}
			
			if (ini != end) {
				pal = false;
				if (ignoreSpaces) { // verifica se estah comparando com espaço, se sim, move indices e assume palindromo
					if (ini == ' ' && end != ' ') {
						i++;
						pal = true;
						continue;
					} else if (end == ' ' && ini != ' ') {
						j--;
						pal = true;
						continue;
					}
				}
			} else {
				pal = true;
			}
			
			i++;
			j--;

		}
		
		System.out.println("'" + word + "'" + " palindrome? (Options: {caseSensitive=" + caseSensitive + ", ignoreSpaces=" + ignoreSpaces + "} " + pal);
	}
	
	public static void main(String[] args) {
		isPalindrome("O romano acata amores a damas amadas e Roma ataca o namoro", false, true);
		isPalindrome("O romano acata amores a damas amadas e Roma ataca o namoro", false, false);
		isPalindrome("O romano acata amores a damas amadas e Roma ataca o namoro", true, true);
		isPalindrome("O romano acata amores a damas amadas e Roma ataca o namoro", true, false);
	}
}
