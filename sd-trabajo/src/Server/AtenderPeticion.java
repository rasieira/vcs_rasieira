package Server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Respositorios.Repositorio;

public class AtenderPeticion implements Runnable {

	private static List<Repositorio> repositorios = new ArrayList<Repositorio>();
	private Socket S;
	public AtenderPeticion(Socket S,List<Repositorio> repositorios )
	{
		this.S=S;
		AtenderPeticion.repositorios=repositorios;
	}
	
	@Override
	public void run() {
		try (InputStreamReader in = new InputStreamReader(S.getInputStream());
				OutputStreamWriter out = new OutputStreamWriter(S.getOutputStream());)
		{
			
			char c;
			String request;
			StringBuilder r = new StringBuilder();
			while((c = (char) in.read()) != '\r') {
				r.append(c);
			}
			
			request = r.toString();

			String[] request_array = request.split(" ");
			if ((!request_array[0].equals("CLONE") 
							&& !request_array[0].equals("ADD")
							&& !request_array[0].equals("PUSH")
							&& !request_array[0].equals("REMOVE")
							&& !request_array[0].equals("LOGIN")))
			{
				out.write("ERROR\r\n");
				out.flush();
				throw new IllegalArgumentException("Formato de comando incorrecto");
			}
			
				if(request_array[0].equals("LOGIN"))
				{
					if(Server.usuariosServer.containsKey(request_array[1]))
					{
						if(Server.usuariosServer.get(request_array[1]).equals(request_array[3]))
						{
						}
						else
						{
						}
					}
					else
					{
					}
				}
				boolean noExiste=true;
				if ((request_array[0].equals("ADD")))
				{
					for(int i=0;i<repositorios.size();i++)
					{
						if(repositorios.get(i).getNombre().equals(request_array[1]))
						{
							noExiste=false;
							out.write("Ya existe un repositorio con ese nombre\r\n");
							out.flush();
							break;
						}
					}
					
				if(noExiste)
					{
						Repositorio repo=new Repositorio(request_array[1]);
						repositorios.add(repo);
						for(int i=0; i<repositorios.size();i++)
						{
							System.out.println(repositorios.get(i).getNombre());
						}
						out.write( request_array[1]+ " ha sido creado\r\n");
					}
				}
				if ((request_array[0].equals("CLONE")))
				{
						Repositorio repo=null;
						for(int i=0;i<repositorios.size();i++)
						{
							if(repositorios.get(i).getNombre().equals(request_array[1]))
							{
								repo=repositorios.get(i);
							}
						}
						ObjectOutputStream oos=new ObjectOutputStream(S.getOutputStream());
						oos.writeObject(repo);
						oos.flush();
						out.flush();
				}
				if ((request_array[0].equals("REMOVE")))
				{
					boolean aux=false;
					for(int i=0;i<repositorios.size();i++)
					{
						if(repositorios.get(i).getNombre().equals(request_array[1])&&(!aux))
						{
							aux=true;
						}
					}
					if (aux)
					{
						List<Repositorio> nombrados = repositorios.stream().filter(n -> n.getNombre().equals(request_array[1])).collect(Collectors.toList());
						repositorios.removeAll(nombrados);
						out.flush();
					} else
					{
						out.write(request_array[1]+"no existe\r\n");
						out.flush();
					}
		
				}
				if((request_array[0].equals("PUSH")))
				{
					
					FileOutputStream f=new FileOutputStream("BDServer\\"+request_array[1]);
					ObjectOutputStream oos=new ObjectOutputStream(f);
					ObjectInputStream ois=new ObjectInputStream(S.getInputStream());
					Repositorio repo = null;
					try {
						repo = (Repositorio) ois.readObject();
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					repositorios.add(repo);
					oos.writeObject(repo);
					oos.flush();
					try {
						oos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Server.repositoriosSerializadosServer.put(repo.getNombre(), "BDServer\\"+repo.getNombre());
					synchronized(Server.RUTA_DE_LA_BD_SERVER) {
						ObjectOutputStream elMap =new ObjectOutputStream(new FileOutputStream(Server.RUTA_DE_LA_BD_SERVER));
						elMap.writeObject(Server.repositoriosSerializadosServer);
						Server.leerBD();
						out.flush();
						elMap.close();
					}
				}
				Server.leerBD();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

