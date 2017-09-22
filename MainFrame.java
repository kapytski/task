import java.io.*;
import java.nio.file.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;

//класс для получения текста из файла
// если текст получить в силу некоторых причин невозможно,
// то в консоль выводятся сообщения об ошибках
// если ошибок нет, то методом getText() возвращается строка String
class GetTextFromFile{
	// аргументы
	// 1. имя файла (по умолчанию используется директория, откуда запущена программа)
	private String filename;
	private Path filepath;
	private byte b[];
		
	// конструктор класса
	GetTextFromFile(String filename){
		this.filename=filename;
	}
	
	// пытаемся получить текст из файла
	String getText() throws IOException{
		// получаем имя файла
		System.out.println("Попытка открыть файл с именем " + filename + ":\n");
		
		// сюда вставляем обработчик имени файла
		
		// удаляем кавычки из имени файла, если таковые имеются
			
		filename=StringManipul.Replace(filename,"(\"|')(.*)(\"|')","$2");
		
		// ---------------------------
		try{
			filepath=Paths.get(filename);
		} catch(InvalidPathException e){System.out.println("Имя файла не конвертируется в путь / Кодировка консоли не походит.\nПроверьте имя файла"); throw e;}
		
		// смотрим, существут ли файл с таким именем
		if(Files.notExists(filepath)) try{
			throw new FileNotFoundException();
		} catch(FileNotFoundException e){System.out.println("Файл не найден / Кодировка консоли не подходит.\nПроверьте имя файла");throw e;}
		
		// читаем байты из файла
		try{
			b=Files.readAllBytes(filepath);
			System.out.println("Прочитано:\n ------------------------------\n");
			// принудительно приводим имя файла к кодировке UTF-8;
			return new String(b,"UTF-8");
		} catch(IOException e){System.out.println("Ошибка чтения\n");throw e;}
		catch(OutOfMemoryError e){System.out.println("Объём файла слишком велик\n");throw e;}
		catch(SecurityException e){System.out.println("Файл не может быть прочитан из-за настроек безопасности\n");throw e;}
				
	}
	
}

// класс, нужный для удаления указанных сиволов в строке
// принимает строку String и массив символов, подлежащих удалению
class StringManipul{
	
	// метод счета числа вхождения паттерна в строку
	public static int PatternCounter(String x,String pattern){
		int times=0;
		Pattern p=Pattern.compile(pattern);
		Matcher m=p.matcher(x);
		// считаем количество вхождений паттернов в строку
		while(m.find()){
			times++;
			m.start();
		}
		return times;
	}
	
	// метод выполнения замены в строке по паттерну
	public static String Replace(String x,String pattern,String replacement){
		Pattern p=Pattern.compile(pattern);
		Matcher m=p.matcher(x);
		if(m.find()){
			return m.replaceAll(replacement);
		} else return x;
	}
		
	public static String Filter(String x){
		String str=x;
		// удаляем знаски препинания и служебные символы
		String patterns1[]={"\\.", "," , "^.", "!", "\\?" , ":", "@", "#", "№", "\\$", "%", "\\\\" , "/", "&", "\\*", "_", 
							"\\)", "\\(", "\\{", "\\}", "\\[", "\\]"};
		for(String pattern: patterns1) str=Replace(str,pattern," ");
		
		// удаляем цифры отдельно стоящие
		String patterns2[]={"\\s*\\d{1,}\\s"};
		for(String pattern: patterns2) str=Replace(str,pattern," ");
		
		return str;
	}
	
	public static void GetStat(String s, String v){
		String fstr=StringManipul.Filter(s);
		String arr1[]=v.split("\\s");
		// System.out.println("До:\n"+fstr);
		
		// удаляем по словарю
		for(String y:arr1){
			//удаляем все, что содержится в словаре и попадается в тексте
			fstr=StringManipul.Replace(fstr,"\\s"+y+"\\s"," ");
		}
		// удаляем множественные пробельные символы
		fstr=StringManipul.Replace(fstr,"(\\s)(\\s{1,})","$1");
		// System.out.println("После:\n" + fstr);
		
		
		// ведем статистику
		// разбиваем строку на подстроки
		String arr2[]=fstr.split("\\s");
		ArrayList<String> result=new ArrayList<String>();
		for(String y:arr2) result.add(y.trim());
		
		// находим уникальные значения
		ArrayList<String> unique=new ArrayList<String>();
		for(int i=0;i<(result.size()-1);i++){
			int counter=1;
			for(int j=i+1;j<result.size();j++){
				if(result.get(i).equals(result.get(j))) counter++;
			}
			if(counter==1) unique.add(result.get(i));
		}
		
		// считаем количество повторов
		for(int i=0;i<unique.size();i++){
			unique.set(i,Collections.frequency(result,unique.get(i))+ "!" + unique.get(i));
		}
		
		//сортируем
		Collections.sort(unique,Collections.reverseOrder());
		
		
		// выводим первых 10 значений
		System.out.println("Частоты встречаемости слов (слово --> частота):");
		for(int i=0;i<unique.size()-1;i++){
			String string=unique.get(i);
			System.out.println("\t" + string.split("!")[1] + "\t-->\t" + string.split("!")[0]);
			if(i>=9) break;
		} 
	}
	
	static boolean AreBracketsCorrect(String s, String obpattern, String cbpattern){
		// работаем со скобками: удаляем все, кроме скобок одного типа
				
		// разбиваем строку на массив из скобок одного типа
		String str[]=StringManipul.Replace(s,"[^" + obpattern + cbpattern + "]","").split("");
		String ob=obpattern.substring(obpattern.length()-1);
		String cb=cbpattern.substring(cbpattern.length()-1);
		//если скобок такого типа нет, то возвращаем true
		if(str[0].equals("")) return true;
		// перекодируем эти скобки следующим образом -- открывающая-1,закрывающая-0
		int b[]=new int[str.length];
		for(int i=0;i<b.length;i++){
			b[i]=(str[i].equals(ob)) ? 1 : 0;
			// System.out.println(b[i] + "\t" + str[i]);
		}
		
		
		// пробегаем по массиву b, смотрим на группы единиц и нулей. Если скобки расставлены верно,
		// то группа из единиц должна сменяться группой из нулей такой же длины
		if(b[0]==0) return false;	// заодно проверяем открывающую скобку
		if(b.length%2!=0) return false;	 // если скобок нечётное количество - выходим
		int i=0;
		int j=i+1;
		int k;
		do{
			
			//проходим по группе единиц и считаем сколько их: когда группа закончится считаем количество 0 аналогичным способом
			int counter1=1;
			while((b[j]==b[i])&(b[i]==1)) {counter1++;j++;}
			// если кобки стоят верно, то количество нулей должной быть равно количеству единиц в группе
			// это количество находится в переменной counter1
			for(k=j;k<j+counter1;){
				if(b[k]==0)k++;else return false;
			}
			i=k;
			j=i+1;
		} while(k<(b.length-1));
		
		
		
		return true;
	}
	
	public static void BCorrect(String s){
		//проверяем, есть вообще скобки
		String str=StringManipul.Replace(s,"[^\\(\\)\\{\\}\\[\\]]","");
		if(str.equals("")) {System.out.println("Скобки в тексте не обнаружены. \n");return;}
		
		//если онивсе-таки есть то проверяем правильность их расставки
		boolean b1=AreBracketsCorrect(s,"\\(","\\)");
		boolean b2=AreBracketsCorrect(s,"\\[","\\]");
		boolean b3=AreBracketsCorrect(s,"\\{","\\}");
		if(b1&b2&b3) System.out.println("correct"); else System.out.println("incorrect");
		
	}
	
	
}

class ActionKinds{
	private static String str;
	
	public static String ReadAttempt(String filename) throws IOException{
					
		// пытаемся считать файл по переданному пути
		try{
			GetTextFromFile gtff=new GetTextFromFile(filename);
			return gtff.getText();
		} catch(Exception e)
		{ // запускаем процедуру по ручному указанию имени файла
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			final String STOP_WORD="->quit";
			
			System.out.println("Ошибка при чтении файла '" + filename + "' :");
			System.out.println("\nВведите имя файла. Для выхода наберите '" + STOP_WORD + "'");
			do{
				str=br.readLine();
				if(str.equals(STOP_WORD)) break;
				try{
					GetTextFromFile gtff=new GetTextFromFile(str);
					return gtff.getText();
				} catch (Exception e1){};
			System.out.println("Для выхода наберите '" + STOP_WORD + "'");
			} while(!str.equals(STOP_WORD));
		}
		return "";
			
		
		
	}
}


class MainFrame{
	public static void main(String args[]) throws IOException{
		String filename=args[0];
		String str;
		String vcblr="russian vcblr.txt";
		
		// получаем строку str с текстом
		str=ActionKinds.ReadAttempt(filename);
				
		// получаем строку со словарями
		vcblr=ActionKinds.ReadAttempt(vcblr);
		
		// получаем статистику документа
		System.out.println("\n Статистика встречаемости:\n");
		StringManipul.GetStat(str,vcblr);
		
		// результат для скобок
		System.out.println("\n Проверка правильности расстановки скобок:\n");
		StringManipul.BCorrect(str);
		
		
	}
}