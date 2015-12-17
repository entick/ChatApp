import java.util.Scanner;

public class FileCommand extends Command {
	String file;
	public FileCommand(String file){
		super(Command.CommandType.FILE);
		this.file=file;
	}
	public String toString(){
		return file;
	}
	public String getFileName(){
		Scanner in = new Scanner(file);
		StringBuffer sb = new StringBuffer();
		String str;
		while (!(str=in.next()).toUpperCase().equals("SIZE")){
			sb.append(str);
		}
		return sb.toString();
	}
}
