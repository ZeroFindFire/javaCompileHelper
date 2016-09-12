import com.google.gson.*;

import java.io.*;
import java.util.*;
public class Cmp{
	public static Gson gson=null;
	public static String get(JsonObject json,String name){
		return gson.fromJson(json.get(name),String.class);
	}
	public static String getPath(JsonArray jpath,Map<String,String> map,String root){
		int l=jpath==null?0:jpath.size();
		String outpath=null;
		for(int j=0;j<l;++j){
			Object obj=jpath.get(j);
			if(obj==null||obj instanceof JsonNull)continue;
			String path=jpath.get(j).getAsString();
			String rp=map.get(path);
			if(rp!=null)path=rp;
			if(root!=null)path=root+"/"+path;
			if(outpath==null)
				outpath=path;
			else
				outpath+=spt+path;
		}
		return outpath;
	}
	public static boolean empty(String str){
		return (str==null||"".equals(str));
	}
	public static String combinePath(String p1,String p2){
		boolean e1=empty(p1);
		boolean e2=empty(p2);
		if(e1||e2){
			if(e1&&e2)return "";
			return e1?p2:p1;
		}
		return p1+spt+p2;
	}
	static char spt=';';
	public static void main(String[] args){
		String source="prj.json";
		String dst="build";
		String sets="settings";
		if(args.length>0){
			source=args[0];
			System.out.println("source: "+source);
			if(args.length>1){
				dst=args[1];
				System.out.println("target: "+dst);
				if(args.length>2){
					sets=args[2];
					System.out.println("settings file: "+sets);
				}else
					System.out.println("used default settings file: "+sets);
			}else
				System.out.println("default target: "+dst);
		}else
			System.out.println("default source: "+source);
		String charset="US-ASCII";
		try{
			File fsrc=new File(source);
			File ffin=new File(sets);
			if(ffin.exists()&&ffin.isFile()){
				FileInputStream fin=new FileInputStream(sets);
				BufferedReader br=new BufferedReader(new FileReader(sets));
				String tmp;
				while((tmp=br.readLine())!=null){
					String[] spts=tmp.split(" ");
					if(spts==null||spts.length!=2)continue;
					if("seperate".equals(spts[0])){
						spt=spts[1].charAt(0);
					}else if("charset".equals(spts[0])){
						charset=spts[1];
						if(empty(charset)){
							charset="US-ASCII";
						}
					}
				}
				br.close();
				System.out.println("settings:");
			}else{
				System.out.println("settings file "+sets+" doesn't exist, used default settings:");
			}
			System.out.println("	seperate character: '"+spt+"', charset: "+charset);
			gson=new GsonBuilder().create();
			Reader reader=new InputStreamReader(new FileInputStream(source),charset);
			JsonObject total=gson.fromJson(reader,JsonObject.class);
			String root=gson.fromJson(total.get("root"),String.class);
			String spts=gson.fromJson(total.get("seperate"),String.class);
			if(!empty(spts)){
				spt=spts.charAt(0);
				System.out.println("	used seperate character "+spt+" setted in source file "+fsrc.getName());
			}
		
			JsonArray jmap=total.getAsJsonArray("map");
			Map<String,String> map = new HashMap<String,String>();
			int l=jmap==null?0:jmap.size();
			for(int i=0;i<l;++i){
				Object obj=jmap.get(i);
				if(obj==null||obj instanceof JsonNull)continue;
				JsonObject ele=(JsonObject)jmap.get(i);
				String name=get(ele,"name");
				String path=get(ele,"path");
				map.put(name,path);
			}
			jmap=total.getAsJsonArray("depends");
			String env_path=getPath(jmap,map,root);
			env_path=combinePath(".",env_path);
			jmap=total.getAsJsonArray("project");
			l=jmap==null?0:jmap.size();
			FileWriter out=new FileWriter(dst);
			for(int i=0;i<l;++i){
				Object obj=jmap.get(i);
				if(obj==null||obj instanceof JsonNull)continue;
				JsonObject ele=(JsonObject)jmap.get(i);
				Boolean Cbuild=gson.fromJson(ele.get("build"),Boolean.class);
				boolean build=Cbuild==null?true:Cbuild;
				if(!build)continue;
				String target=get(ele,"target");
				JsonArray ja=ele.getAsJsonArray("depends");
				int len=ja==null?0:ja.size();
				out.write("javac "+target);
				String path=getPath(ja,map,root);
				path=combinePath(env_path,path);
				out.write(" -cp "+path+"\n");
			}
		
			out.close();
			reader.close();
			System.out.println("work done");
		
			System.out.println("run the "+new File(dst.trim()).getName()+" to do really building");
		}catch(Exception e){
			System.out.println("error");
			e.printStackTrace();
		}
		
	}
}