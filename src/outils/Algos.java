package outils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import constructionEcoles.Agglomeration;
import constructionEcoles.Ville;




/**
 * Classe donnant l'accès à plusieurs algorithmes permettant de minimiser plus ou moins bien le nombre d'écoles d'une agglomération.
 * @author Yann Trividic
 * @version 1.0
 */

// TODO On en arrive au point où le manque de visualisation commence à se faire sentir, 
// pour installer JavaFX, voir https://www.eclipse.org/forums/index.php/t/1097505/

// TODO Etant donné que fractionner la classe Agglo en deux implique de changer la visibilité de certaines méthodes
// peut-être qu'il serait mieux que la classe Main soit dans un package complètement à part (afin que les protected ait du sens).
// ou alors mettre la classe Algos dans le package outils et mettre toutes les méthodes d'Agglo en public

public class Algos {
	
	/**
	 * Méthode statique permettant de retirer un certain nombre d'écoles à l'agglomération passée en argument.
	 * Cet algorithme est très naïf et très améliorable.
	 * @see		Agglomeration algorithmeApproximationUnPeuMoinsNaif(int, Agglomeration)
	 * @param 	k	nombre d'itérations de l'algorithme.
	 * @param	agg	l'agglomération dont on cherche à réduire le nombre d'écoles. 
	 * @return	la même agglomération que celle passée en argument, mais avec une configuration d'écoles différentes
	 */
	public static Agglomeration algorithmeApproximationNaif(int k, Agglomeration agg) {
		ArrayList<Ville> villes = (ArrayList<Ville>) agg.getVilles() ;
		for(int i = 0 ; i < k ; i++) {
			Ville v = villes.get((int) Math.random()*villes.size()) ;
			try {
				if(v.getHasEcole()) {
					agg.retirerEcole(v);
				} else agg.ajouterEcole(v) ;
			} catch(Exception e) {
				System.out.println(e) ;
			}
		} 
		return agg;
	}
	
	/**
	 * Méthode statique permettant de retirer un certain nombre d'écoles à l'agglomération passée en argument.
	 * Cet algorithme est moins naïf que l'autre, mais toujours améliorable.
	 * @see		Agglomeration algorithmeApproximationNaif(int, Agglomeration)
	 * @param 	k	nombre d'itérations de l'algorithme.
	 * @param 	agg	l'agglomération dont on cherche à réduire le nombre d'écoles. 
	 * @return	la même agglomération que celle passée en argument, mais potentiellement avec beaucoup moins d'écoles
	 */
	public static Agglomeration algorithmeApproximationUnPeuMoinsNaif(int k, Agglomeration agg) {
		ArrayList<Ville> villes = (ArrayList<Ville>) agg.getVilles() ;
		int scoreCourant = agg.nbEcoles();
		for(int i = 0 ; i < k ; i++) {
			Ville v = villes.get((int) Math.random()*villes.size()) ;
			try {
				if(v.getHasEcole()) {
					agg.retirerEcole(v);
				} else agg.ajouterEcole(v) ;
			} catch(Exception e) {
				System.out.println(e) ;
			}
			if(agg.nbEcoles() < scoreCourant) {
				i = 0 ;
				scoreCourant = agg.nbEcoles() ;
			}
		} 
		return agg;
	}

	
	/**
	 * Methode de la classe permettant de minimiser le mieux le nombre d'écoles d'une agglomération donnée avec des écoles déjà construites ou non.
	 * Dans un premier temps, on va parcourir toutes les villes et placer des ecoles dans les villes qui devront forcément en accueillir.
	 * Cela concerne toutes les villes voisines de villes de degre 1 (c'est-a-dire les villes accessibles uniquement via une seule route).
	 * Ce faisant, on ajoute dans une file de priorité les villes de degré supérieur à 1. Une fois ce premier parcours effectué, on vérifie
	 * que les villes contenues dans la file n'ont pas d'écoles. Si elles en ont, on les enlève.
	 * Dans un second temps, tant que la contrainte d'Accessibilité n'est pas remplie, on extrait de la file de priorité la ville pour laquelle la 
	 * construction d'une école bénéficierait le plus à l'agglomération, puis on actualise la file.
	 * Au sortir de cet étape, le nombre d'écoles de l'agglomération est relativement proche du résultat optimal, voire est le résultat optimal.
	 * @param agg						l'agglomération dont on cherche à réduire le nombre d'écoles. Le graphe qui la représente peut-être non-connexe et orienté.
	 * @param garderEcolesConstruites	booléen permettant de préciser si on souhaite garder ou non les écoles déjà présentes dans l'agglomération
	 * @return							l'agglomération donnée en argument avec un nombre d'école minimisé
	 */
	public static Agglomeration algorithmeFilePriorite(Agglomeration agg, boolean garderEcolesConstruites) {
		// TODO tester la validité de cet algorithme, sa complexité et vérifier empiriquement s'il permet de trouver le résultat optimal à chaque fois
		// ^^^^^ à faire dans la classe Tester
		
		if(!garderEcolesConstruites) agg.clearEcole(); 	// on enlève toutes les écoles présentes. L'algorithme fonctionne si on décide de garder les écoles
														// déjà construites. Le nombre d'écoles final risque cependant d'être supérieur.
		
		ArrayList<Ville> villes = (ArrayList<Ville>) agg.getVilles();
				
		// On utilise une PriorityQueue dont le critère de comparaison est le nombre de villes qui bénéficieraient de l'ajout d'une ville dans la ville courante
		PriorityQueue<Ville> p = new PriorityQueue<Ville>(villes.size(), Comparator.comparing(Ville::beneficeSiAjoutEcole).reversed());
		// Pour randomiser les tests, si les algorithmes des PriorityQueue sont totalement déterministes,
		// on peut modifier beneficeSiAjoutEcole en ajoutant un bruit aléatoire à la valeur retournée (voir le commentaire dans la méthode citée)
		
		for(Ville v : villes) { 							// cette boucle met une école dans les villes qui devront forcément contenir une école
			ArrayList<Ville> voisins;
			if((voisins = v.getVoisins()).size() == 1) { 	// c'est-à-dire les villes qui n'ont qu'un voisin et qui n'ont pas déjà d'école construite
				if(!v.getHasEcole()) voisins.get(0).setHasEcole(true);		// le 2e if sert dans le cas d'une agglomération de deux villes 
			} else {														// ou qu'on a choisi de garder les écoles déjà construites
				p.add(v); 									// si elles ont plus d'un voisin, on les ajoute dans la file de priorité
			}
		}
		
		// Arrivés à ce point, soit on décide d'enlever toutes les villes ayant déjà des écoles construites avec cette ligne :
		// p.removeIf((v) -> v.getHasEcole());
		// soit on décide de garder les villes avec des écoles construites dans la file. La file aura donc plus d'éléments
		// et les opérations pour réévaluer les ordres de priorité seront plus couteuses.
		// Selon la forme du graphe, les deux choix peuvent être valides. S'il y a beaucoup de villes de degré 1,
		// alors cela vaudrait le coup de retirer toutes les villes à écoles, sinon, cela ne vaut pas le coup.
		// Pour l'instant, on va supposer que le nombre de villes de degré 1 est négligeable par rapport au nombre de villes total de l'agglo.
		// On pourrait potentiellement améliorer la complexité en calculant le ratio nbVillesDeg1/nbVilles marquant la limite entre l'efficacite de
		// la première méthode et l'efficacité de la seconde.
		
		while(!agg.respecteAccessibilite()) {		// La file ne sera jamais vide alors que la contrainte d'accessibilité n'est pas respectée ;
			Ville v = p.poll();						// tant que la contrainte d'accessibilité n'est pas respectée ...
			if(!v.getHasEcole()) {					// ... on défile et on ajoute une école dans la ville défilée si elle n'en a pas déjà une. Sinon, on ne fait rien.
				v.setHasEcole(true);
				if(!p.isEmpty()) p.add(p.remove());	// permet d'actualiser la file de priorité selon les nouveaux résultats du comparateur, 
			} 										// c'est çe qui est vraiment coûteux en temps de calcul dans l'algorithme donc on le fait uniquement quand la file doit
		}											// être actualisée. La complexité est apparemment améliorable avec un tas de Fibonacci
													// voir https://stackoverflow.com/questions/1871253/updating-java-priorityqueue-when-its-elements-change-priority
		return agg;									
	}
	
		/*
		Seconde idée d'algo :
		
				Representer le graphe sous la forme d'une matrice d'adjacence
				La solution de la répartition des écoles se trouverait en résolvant un système de type MX = (ai) avec a_i * x_i >= 1 et la somme des x_i est minimale
				avec M la matrice d'adjacence, X un vecteur contenant des 0 ou des 1 (école ou pas école).
				Le fait que tous les termes de MX soient non nuls signifie que la contrainte d'accessibilité est respectée.
				Le fait que la somme des a_i est minimale signifie que la contrainte d'économie est respectée.
				
				Question : comment minimiser X ?
				
				Alternativement, utiliser la fonction quadratique associée à M puis résoudre le système obtenu ?
		
		
		Troisieme idée d'algo :
			
				Travailler avec une forme "minimale" et bipartite du graphe. C'est-à-dire que le graphe serait divisé en deux sous-graphes O et N tels que 
				O représente l'ensemble des sommets possédant une école et N l'ensemble des sommets ne possédant pas d'écoles.
				O doit contenir le moins d'éléments possible, et N le plus d'éléments possible : contrainte d'économie
				le graphe est bipartite : contrainte d'accessibilité
				
				Question : comment minimiser le graphe bipartite ? 
				D'après https://stackoverflow.com/questions/20107645/minimizing-number-of-crossings-in-a-bipartite-graph,
				ce genre de problème est NP-difficile
		*/
		
}
