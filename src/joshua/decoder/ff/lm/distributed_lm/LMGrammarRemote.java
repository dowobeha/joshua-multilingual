/* This file is part of the Joshua Machine Translation System.
 * 
 * Joshua is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package joshua.decoder.ff.lm.distributed_lm;

import joshua.decoder.Support;
import joshua.decoder.Symbol;
import joshua.decoder.ff.lm.LMGrammar;
import joshua.util.FileUtility;

import java.io.BufferedReader;
import java.util.Hashtable;

/**
 * this class implement 
 * (1) get the list of lm servers
 * (2) setup network connection
 * (3) get lm probablity for n-gram remotely 
 * 
 * @author Zhifei Li, <zhifei.work@gmail.com>
 * @version $LastChangedDate: 2008-10-02 02:02:16 -0400 (星期四, 02 十月 2008) $
 */
//PATH: this => LMClient => network => LMServer => LMGrammar => LMGrammar_JAVA/SRILM; and then reverse the path
public class LMGrammarRemote  extends LMGrammar {
	
//	if remote method is used	
	private LMClient p_lm_client=null;
	//LMClient p_suffix_client=null;
	
	//!!! we assume both suffix and lm are remoted, if one is remoted
	public LMGrammarRemote(Symbol psymbol, int order, String f_server_lists, int num_servers){
		super(psymbol, order);
	
		System.out.println("use remote suffix and lm server");
		String[] hosts =new String[num_servers];
		int[] ports =new int[num_servers];
		double[] weights =new double[num_servers];
		
		read_lm_server_lists(f_server_lists,num_servers, hosts,ports,weights);
		if(num_servers==1){
			p_lm_client = new LMClientSingle(hosts[0], ports[0]);
/*
			//debug begin
			System.out.println("begin to call msrlm");
			System.out.println("prob is " + ((LMClient_Single)p_lm_client).get_prob_msrlm("i am ok", 3));
			System.out.println("prob is " + ((LMClient_Single)p_lm_client).get_prob_msrlm("china is with", 3));
			System.out.println("prob is " + ((LMClient_Single)p_lm_client).get_prob_msrlm("china is a", 3));
			System.out.println("prob is " + ((LMClient_Single)p_lm_client).get_prob_msrlm("the chinese government", 3));

			System.exit(0);
			//end
*/
		}
		else
			p_lm_client = new LMClientMultiServer(hosts, ports, weights, num_servers);
	}	
	
	public void end_lm_grammar(){
		p_lm_client.close_client();
	}
	
	
//	format: lm_file host port weight
	private void read_lm_server_lists(String f_server_lists, int num_servers, String[] l_lm_server_hosts, int[] l_lm_server_ports, double[] l_lm_server_weights ){			
		BufferedReader t_reader = FileUtility.getReadFileStream(f_server_lists);
		String line;
		int count=0;
		while((line=FileUtility.read_line_lzf(t_reader))!=null){
			String fname = line.trim();
			Hashtable res_conf = read_config_file(fname);
			
			String lm_file=(String) res_conf.get("lm_file");
			String host = (String) res_conf.get("hostname");
			int port = (Integer) res_conf.get("port");
			double weight = (Double) res_conf.get("weight");
			
			l_lm_server_hosts[count]=host;
			l_lm_server_ports[count]=port;
			l_lm_server_weights[count]=weight;
			count++;
			System.out.println("lm server: " + "lm_file: " + lm_file +"; host: " + host + "; port: " + port + "; weight: " + weight);
		}
		if(count!=num_servers){
		    System.out.println("num of lm servers does not match");
		    System.exit(1);
		}
		FileUtility.close_read_file(t_reader);
	}
	
	private static Hashtable  read_config_file(String config_file){
		Hashtable res =new Hashtable();
		BufferedReader t_reader_config = FileUtility.getReadFileStream(config_file);
		String line;
		while((line=FileUtility.read_line_lzf(t_reader_config))!=null){
			//line = line.trim().toLowerCase();
			line = line.trim();
			if(  (line.matches("^\\s*\\#.*$")==true)//comment line
			   || (line.matches("^\\s*$")==true))//empty line
				continue;
			
			if(line.indexOf("=")!=-1){//parameters
				String[] fds = line.split("\\s*=\\s*");
				if(fds.length!=2){
					Support.write_log_line("Wrong config line: " + line, Support.ERROR);
					System.exit(1);
				}
				if(fds[0].compareTo("lm_file")==0){
					String lm_file = fds[1].trim();
					res.put("lm_file", lm_file);
					System.out.println(String.format("lm file: %s", lm_file));
				}else if(fds[0].compareTo("remote_lm_server_port")==0){
					int port = new Integer(fds[1]);
					res.put("port", port);
					System.out.println(String.format("remote_lm_server_port: %s", port));					
				}else if(fds[0].compareTo("hostname")==0){
					String host_name = fds[1].trim();
					res.put("hostname", host_name);
					System.out.println(String.format("host name is: %s", host_name));					
				}else if(fds[0].compareTo("interpolation_weight")==0){
					double interpolation_weight = new Double(fds[1]);
					res.put("weight", interpolation_weight);
					System.out.println(String.format("interpolation_weightt: %s", interpolation_weight));					
				}else{
					Support.write_log_line("Warning: not used config line: " + line, Support.ERROR);
					//System.exit(1);
				}
			}
		}
		FileUtility.close_read_file(t_reader_config);
		return res;
	}
	
	
	
	//format: lm_file host port weight
	private void read_lm_server_lists_old(String f_server_lists, int num_servers, String[] l_lm_server_hosts, int[] l_lm_server_ports, double[] l_lm_server_weights ){			
		BufferedReader t_reader = FileUtility.getReadFileStream(f_server_lists);
		String line;
		int count=0;
		while((line=FileUtility.read_line_lzf(t_reader))!=null){
			String[] fds = line.split("\\s+");
			if(fds.length!=4){
			    System.out.println("read index, bad line: " + line);
			    System.exit(1);
			}
			String lm_file=fds[0];
			String host = fds[1];
			int port = (new Integer(fds[2])).intValue();
			double weight = (new Double(fds[3])).doubleValue();
			l_lm_server_hosts[count]=host;
			l_lm_server_ports[count]=port;
			l_lm_server_weights[count]=weight;
			count++;
			System.out.println("lm server: " + "lm_file: " + lm_file +"; host: " + host + "; port: " + port + "; weight: " + weight);
		}
		if(count!=num_servers){
		    System.out.println("num of lm servers does not match");
		    System.exit(1);
		}
		FileUtility.close_read_file(t_reader);
	}
	
//	read grammar locally by the Java implementation
	public void read_lm_grammar_from_file(String grammar_file){
	    System.out.println("read remoate lm, do nothing");
	}
	
	
    //this should be called by decoder only
   protected double get_prob_specific(int[] ngram_wrds, int order, boolean check_bad_stuff){
       return p_lm_client.get_prob(ngram_wrds, ngram_wrds.length);
   }
		
   public void write_vocab_map_srilm(String fname){
		System.out.println("Error: call write_vocab_map_srilm in remote, must exit");
		System.exit(1);
	}
	
   protected double get_prob_backoff_state_specific(int[] ngram_wrds, int order, int n_additional_bow){
	    System.out.println("Error: call get_prob_backoff_state_specific in LMGrammar_REMOTE, must exit");
		System.exit(1);
		return -1;
	   //return p_lm_client.get_prob_backoff_state(ngram_wrds, n_additional_bow);
	}

   public int[] get_left_equi_state(int[] original_state, int order, double[] cost){
	   return original_state;
	   /*if(Decoder.use_left_euqivalent_state==false)
			return original_state;
	   return p_lm_client.get_left_euqi_state(original_state, order, cost);*/
   }
   
   public int[] get_right_equi_state(int[] original_state, int order, boolean check_bad_stuff){
	   return original_state;
	   /*if(Decoder.use_right_euqivalent_state==false)
			return original_state;
	   return p_lm_client.get_right_euqi_state(original_state,  order);*/
   }
   
   public int replace_with_unk(int in){
	   System.out.println("Error: call replace_with_unk in LMGrammar_REMOTE, must exit");
	   System.exit(1);
	   return 0;
   }
	   
}
