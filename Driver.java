import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Stack;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Driver {
	
	public static void main(String[] args) throws Exception {
		
		
		//Standard input way as in book
		ANTLRInputStream input = new ANTLRInputStream(System.in);
		
		LittleLexer lexer = new LittleLexer(input);
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		LittleParser parser = new LittleParser(tokens);
		
		parser.removeErrorListeners();
		
		parser.addErrorListener(new VerboseListener());
		
		ParseTree tree = parser.program();
		
		ParseTreeWalker walker = new ParseTreeWalker();
		
		walker.walk(new SymbolExtractor(), tree);
		
		System.out.println();
		
		
	}
	
	static class SymbolExtractor extends LittleBaseListener {
		
		private Stack<SymbolTable> symbol_table_stack;
		private Stack<SymbolTable> symbol_table_stack_seen;
		private SymbolTable current_table;
		private int block_count;
		private Queue<String> tinyCode;
		//private ArrayList<String> IRCode;
		private Queue<Node<String>> ASTs;
		private Queue<String> IRCode;
		//----------------
		// private Stack<Node<String>> AST_stack;
		
		public SymbolExtractor() {
			this.tinyCode = new LinkedList<String>();
			//this.IRCode = new ArrayList<String>();
			this.IRCode = new LinkedList<String>();
			this.ASTs = new LinkedList<Node<String>>();
			this.symbol_table_stack = new Stack<SymbolTable>();
			this.symbol_table_stack_seen = new Stack<SymbolTable>();
			this.block_count = 0;
			this.current_table = null;

		}
		
		
		// Program Declarations _________________________________________
		@Override 
		public void enterProgram(LittleParser.ProgramContext ctx) { 
			
			this.symbol_table_stack.push(new SymbolTable("GLOBAL"));
			this.current_table = this.symbol_table_stack.peek();
			
		}
		
		@Override 
		public void exitProgram(LittleParser.ProgramContext ctx) {
			

			
			
			int size_stack = symbol_table_stack.size();
			
			for(int pop = 0; pop < size_stack; pop++){
			
				this.symbol_table_stack_seen.push(this.symbol_table_stack.pop());
				
			}
			
			
			
			// For loop to iterate through print stack
			
			int size = symbol_table_stack_seen.size();
			
			for (int stack_iterate = 0; stack_iterate < size; stack_iterate++) {
				
				
				SymbolTable current = symbol_table_stack_seen.pop();
				
				// Print current scope
				// System.out.println("\n;LABEL " + current.getScope());
				if(!current.getScope().contains("GLOBAL")){
					IRCode.add(";IR Code");
					IRCode.add(";LABEL " + current.getScope());
					IRCode.add(";LINK");
				}
				
				// Iterate through arraylist to print symbols in current symbol table
				for(int array_iterate = 0; array_iterate < current.symbolNames.size(); array_iterate++) {
					
					String symbol = current.symbolNames.get(array_iterate);
					
					SymbolAttributes value = current.symbolTable.get(symbol);
					
					// If the type is a string, print name, type and value 
					// Else print name and type for other symbols
					if(value.gethetype() == "STRING") {
						tinyCode.add("str " + symbol  + " " + value.getValue());
					}
					
					else {
						
						tinyCode.add("var " + symbol);
					
					}
					
				}
				
			}
			//############################################## PROJECT 4 CODE BELOW #####################################################
			int tempCounter = 1;

			SymbolTable optimize = null;

			// for(int node = 0; node < ASTs.size(); node++){
			//loop through list of ast's
			while(!ASTs.isEmpty()){	
				Node<String> current_tree = ASTs.remove();

				if(current_tree.getData().contains("READ")){						//check instruction type
					//Node<String> first_child = current_tree.getChild();
					String type = current_tree.getChild().getData();
					String name = current_tree.getChild().getChild().getData();

					if(type.contains("INT")){
						IRCode.add(";READI " + name);
					}
					else if(type.contains("FLOAT")){
						IRCode.add(";READF " + name);
					}
					else{
						IRCode.add(";READS " + name);
					}
				}
				
				else if(current_tree.getData().contains("WRITE")){					//check instruction type
					//Node<String> first_child = current_tree.getChild();
					String type = current_tree.getChild().getData();
					String name = current_tree.getChild().getChild().getData();
					
					//System.out.println("TYPE AND NAME: " + type + " " + name+"\n\n");

					if(type.contains("INT")){
						IRCode.add(";WRITEI " + name);
					}
					else if(type.contains("FLOAT")){
						IRCode.add(";WRITEF " + name);
					}
					else if(type.contains("STRING")){
						IRCode.add(";WRITES " + name);
					}
				}
				else if(current_tree.getData().contains(":=")){						//check instruction type
					String thetype = current_tree.getChild().getData();
					//System.out.println("\n\nTYPE: " + thetype + "\n\n");
					String funct = current_tree.getChild().getChild().getData();
					String equals = current_tree.getChild().getChild().getChild().getData();
					//System.out.print("\n\nHERE: " +equals);

					if(current_tree.getChild().getChild().getChild().getChild() != null){ //:= -> type -> function -> equals -> op2 -> op1
						String Oneop = current_tree.getChild().getChild().getChild().getChild().getData();
						String Secop = current_tree.getChild().getChild().getChild().getChild().getChild().getData();

						if(funct.contains("+")){																			//check operation type
							if(thetype.contains("INT")){
								IRCode.add(";ADDI " + Secop + " " + Oneop + " " + "$T" + tempCounter);
								IRCode.add(";STOREI " + "$T" + tempCounter + " " + equals);
								tempCounter++;
							}
							else if(thetype.contains("FLOAT")){
								IRCode.add(";ADDF " + Secop + " " + Oneop + " " + "$T" + tempCounter);
								IRCode.add(";STOREF " + "$T" + tempCounter + " " + equals);
								tempCounter++;
							}
						}
						else if(funct.contains("-")){																		//check instruction type
							if(thetype.contains("INT")){
								IRCode.add(";SUBI " + Secop + " " + Oneop + " " + "$T" + tempCounter);
								IRCode.add(";STOREI " + "$T" + tempCounter + " " + equals);
								tempCounter++;
							}
							else if(thetype.contains("FLOAT")){
								IRCode.add(";SUBF " + Secop + " " + Oneop + " " + "$T" + tempCounter);
								IRCode.add(";STOREF " + "$T" + tempCounter + " " + equals);
								tempCounter++;
							}
						}
						else if(funct.contains("*")){																		//check instruction type
							if(thetype.contains("INT")){
								if(Secop.contains("(")){
									Secop = Secop.replace("(", "");
									Oneop = Oneop.replace(")", "");
								}
								IRCode.add(";MULTI " + Secop + " " + Oneop + " " + "$T" + tempCounter);
								IRCode.add(";STOREI " + "$T" + tempCounter + " " + equals);
								tempCounter++;
							}
							else if(thetype.contains("FLOAT")){
								IRCode.add(";MULTF " + Secop + " " + Oneop + " " + "$T" + tempCounter);
								IRCode.add(";STOREF " + "$T" + tempCounter + " " + equals);
								tempCounter++;
							}
						}
						// if it contains "/" --> DIVIDE
						else{
							try{
								int First = Integer.parseInt(Oneop);
								
								if(thetype.contains("INT")){
									String tempreg = "$T" + tempCounter;
									tempCounter++;
									
									IRCode.add(";STOREI " + Oneop + " " + tempreg);
									IRCode.add(";DIVI " + Secop + " " + tempreg + " " + "$T" + tempCounter);
									IRCode.add(";STOREI " + "$T" + tempCounter + " " + equals);
									tempCounter++;
								}
								
							}
							catch(Exception e){
								try{

									float First = Float.parseFloat(Oneop); 
									if(thetype.contains("FLOAT")){
										String tempreg = "$T" + tempCounter;
										tempCounter++;

										IRCode.add(";STOREF " + Oneop + " " + tempreg);
										IRCode.add(";DIVF " + Secop + " " + tempreg + " " + "$T" + tempCounter);
										IRCode.add(";STOREF " + "$T" + tempCounter + " " + equals);
										tempCounter++;
									}
								}
								catch(Exception f){
									if(thetype.contains("INT")){
										IRCode.add(";DIVI " + Secop + " " + Oneop + " " + "$T" + tempCounter);
										IRCode.add(";STOREI " + "$T" + tempCounter + " " + equals);
										tempCounter++;
									}
									else if(thetype.contains("FLOAT")){
										IRCode.add(";DIVF " + Secop + " " + Oneop + " " + "$T" + tempCounter);
										IRCode.add(";STOREF " + "$T" + tempCounter + " " + equals);
										tempCounter++;
									}
								}
							}
						}
					}
					else if(current_tree.getChild().getChild().getChild() != null){
	
						try{
							int Second = Integer.parseInt(equals);
							
							if(thetype.contains("INT")){
								String tempreg = "$T" + tempCounter;
								//tempCounter++;
	
								IRCode.add(";STOREI " + equals + " " + tempreg);
								IRCode.add(";STOREI " + tempreg + " " + funct);
								tempCounter++;
							}
							
						}
						catch(Exception e){
	
							float Second = Float.parseFloat(equals); 
							if(thetype.contains("FLOAT")){
								String tempreg = "$T" + tempCounter;
								//tempCounter++;
	
								IRCode.add(";STOREF " + equals + " " + tempreg);
								IRCode.add(";STOREF " + tempreg + " " + funct);
								tempCounter++;
							}
								
						}
					}
				}
				
			}

			IRCode.add(";RET");
			IRCode.add(";tiny code");
			System.out.println("\n##### IR CODE #####");
			//System.out.print(IRCode);
			int currRegister = 0;
			while(!IRCode.isEmpty()){
				
				//save curr node in string
				String curr_IR_node = IRCode.remove(); 
				//System.out.println(curr_IR_node);
				//if not translated to tiny
				if(curr_IR_node.contains(";IR Code")) System.out.println(curr_IR_node);
				else if(curr_IR_node.contains(";LABEL main")) System.out.println(curr_IR_node);
				else if(curr_IR_node.contains(";LINK")) System.out.println(curr_IR_node);
				else if(curr_IR_node.contains(";RET")) System.out.println(curr_IR_node);
				else if(curr_IR_node.contains(";tiny code")) System.out.println(curr_IR_node);
				//else convert to tiny
				else{

					//Print IR node first
					System.out.println(curr_IR_node);
					
					// ...then process

					//Remove semicolon from start of IR node to uncomment
					curr_IR_node.replace(";", "");

					//Split the string by space
					//i.e curr_IR_node = "STOREI 1 $T1" --> process = ["STOREI", "1", "$T1"]
					String[] process = curr_IR_node.split(" "); 

					if(process[0].contains("ADDI")){
						tinyCode.add("move " + process[1] + " r" + currRegister);//make symbolTabme[process[1]] = "r" + currRegister
						tinyCode.add("addi " + process[2] + " r" + currRegister);
						//currRegister++;

					}
					else if(process[0].contains("ADDF")){
						tinyCode.add("move " + process[1] + " r" + currRegister);//make symbolTabme[process[1]] = "r" + currRegister
						tinyCode.add("addr " + process[2] + " r" + currRegister);
						//currRegister++;

					}
					else if(process[0].contains("SUBI")){
						tinyCode.add("move " +  process[1] + " r" + currRegister);//make symbolTabme[process[1]] = "r" + currRegister
						tinyCode.add("subi " +  process[2] + " r" + currRegister);
						//currRegister++;
					}
					else if(process[0].contains("SUBF")){
						tinyCode.add("move " + process[1] + " r" + currRegister);//make symbolTabme[process[1]] = "r" + currRegister
						tinyCode.add("subr " +  process[2] + " r" + currRegister);
						//currRegister++;
					}
					else if(process[0].contains("MULTI")){
						tinyCode.add("move " + process[1] + " r" + currRegister);//make symbolTabme[process[1]] = "r" + currRegister
						tinyCode.add("muli " + process[2] + " r" + currRegister);
						//currRegister++;
					}
					else if(process[0].contains("MULTF")){
						tinyCode.add("move " + process[1] + " r" + currRegister);//make symbolTabme[process[1]] = "r" + currRegister
						tinyCode.add("mulr " + process[2] + " r" + currRegister);
						//currRegister++;

/*
						if(process[2].contains("$T")){
							int savefloatreg = currRegister; // register for 2.0 
							currRegister++;//increments register for z
							tinyCode.add("move " + process[1] + " r" + currRegister); //z -----------------------SYMBOL TABLE ENTRY with the process[1] and the register -> setValue
							tinyCode.add("divi " + "r" + savefloatreg + " r" + currRegister);
							//currRegister++;
						}else{
							tinyCode.add("move " + process[1] + " r" + currRegister);//make symbolTabme[process[1]] = "r" + currRegister
							try{
								
								int temp = Integer.parseInt(process[2]);
								int regTemp = currRegister;
								currRegister++;
								tinyCode.add("move " + process[2] + " r" + currRegister);//make symbolTabme[process[2]] = "r" + currRegister
								tinyCode.add("divi " + " r" + currRegister + " r" + regTemp);

							}catch(Exception x){
								tinyCode.add("divi " + process[2] + " r" + currRegister);
								//currRegister++;
							}
						}*/


					}
					//	;STOREF 2.0 $T4  process = ['STOREF', '2.0', '$T4']
					//  ;DIVF z $T4 $T5 process = ['DIVF', 'z', '$T4', '$T5']
					else if(process[0].contains("DIVF")){
						if(process[2].contains("$T")){
							int savefloatreg = currRegister; // register for 2.0 
							currRegister++;//increments register for z
							tinyCode.add("move " + process[1] + " r" + currRegister); //z -----------------------SYMBOL TABLE ENTRY with the process[1] and the register -> setValue
							tinyCode.add("divr " + "r" + savefloatreg + " r" + currRegister);
							//currRegister++;
						}
						else{
							tinyCode.add("move " + process[1] + " r" + currRegister); //[DIVF, z, y, $T6] -------------------------------USE SYMBOL TABLE HERE
							/*

							process[1] - findEntry getValue->r5

							*/
							try{
								
								int temp = Integer.parseInt(process[2]);
								int regTemp = currRegister;
								currRegister++;
								tinyCode.add("move " + process[2] + " r" + currRegister);//make symbolTabme[process[2]] = "r" + currRegister
								tinyCode.add("divr " + " r" + currRegister + " r" + regTemp);

							}
							catch(Exception x){
								tinyCode.add("divr " + process[2] + " r" + currRegister);
								//currRegister++;
							}
						}
					}
					else if(process[0].contains("DIVI")){ //a := b/c

						if(process[2].contains("$T")){
							int savefloatreg = currRegister; // register for 2.0 
							currRegister++;//increments register for z
							tinyCode.add("move " + process[1] + " r" + currRegister); //z -----------------------SYMBOL TABLE ENTRY with the process[1] and the register -> setValue
							tinyCode.add("divi " + "r" + savefloatreg + " r" + currRegister);
							//currRegister++;
						}else{
							tinyCode.add("move " + process[1] + " r" + currRegister);//make symbolTabme[process[1]] = "r" + currRegister
							try{
								
								int temp = Integer.parseInt(process[2]);
								int regTemp = currRegister;
								currRegister++;
								tinyCode.add("move " + process[2] + " r" + currRegister);//make symbolTabme[process[2]] = "r" + currRegister
								tinyCode.add("divi " + " r" + currRegister + " r" + regTemp);

							}catch(Exception x){
								tinyCode.add("divi " + process[2] + " r" + currRegister);
								//currRegister++;
							}
						}
					}
					else if(process[0].contains("STOREI")){ //--------------------------------IN THE SYMBOL TABLE, kill variable stored in process[2] --> value = store register
						
						if (process[1].contains("$")){
							tinyCode.add("move " + "r" + currRegister  + " " + process[2]);
							currRegister++;
						}
						else if (process[2].contains("$")){	
							tinyCode.add("move " + process[1] + " r" + currRegister);
								
						}

					}
					else if(process[0].contains("STOREF")){
						if (process[1].contains("$")){
							tinyCode.add("move " + "r" + currRegister + " " + process[2]);
							currRegister++;
						}
						else if (process[2].contains("$")){
							try {
								float temp = Float.parseFloat(process[2]);
								tinyCode.add("move " + process[1] + " r" + currRegister);
								currRegister++;
							}
							catch(Exception x){
								tinyCode.add("move " + process[1] + " r" + currRegister);
							}
						}
					}
					else if(process[0].contains("READI")){
						tinyCode.add("sys readi " +  process[1]);
					}
					else if(process[0].contains("READF")){
						tinyCode.add("sys readr " + process[1]);
					}
					else if(process[0].contains("READS")){
						tinyCode.add("sys reads " + process[1]);
					}
					else if(process[0].contains("WRITEI")){
						tinyCode.add("sys writei " + process[1]);
					}
					else if(process[0].contains("WRITEF")){
						tinyCode.add("sys writer " + process[1]);
					}
					else if(process[0].contains("WRITES")){
						tinyCode.add("sys writes " + process[1]);
					}

					
				}
				
				
			}
			tinyCode.add("sys halt");
			while(!tinyCode.isEmpty()){
				System.out.println(tinyCode.remove());
			}
			//############################################## PROJECT 4 CODE ABOVE #####################################################
		}
			
		
		
		
		// String Declarations _____________________________________________
		@Override 
		public void enterString_decl(LittleParser.String_declContext ctx) { 
			
			this.current_table.addSymbol(ctx.id().IDENTIFIER().getText(), new SymbolAttributes("STRING", ctx.str().STRINGLITERAL().getText()));
		}
		
		@Override 
		public void exitString_decl(LittleParser.String_declContext ctx) { 
		
		
		}
		
		// Variable Declarations ____________________________________
		@Override 
		public void enterVar_decl(LittleParser.Var_declContext ctx) { 
			String type = ctx.var_type().getText();
			
			String id_list_string = ctx.id_list().getText();
			String[] id_list = id_list_string.split(",");
			
			for(int id_iterate = 0; id_iterate < id_list.length; id_iterate++) {
				
				this.current_table.addSymbol(id_list[id_iterate], new SymbolAttributes(type, ""));
				
			}
		}

		@Override 
		public void exitVar_decl(LittleParser.Var_declContext ctx) { 
		
		
		}
		
		// Function Parameter List Declaration __________________________
		
		@Override public void enterParam_decl_list(LittleParser.Param_decl_listContext ctx) { }
	
		@Override public void exitParam_decl_list(LittleParser.Param_decl_listContext ctx) { }
		

		// Function Parameter Declarations ______________________________
		@Override 
		public void enterParam_decl(LittleParser.Param_declContext ctx) { 
			
			String type = ctx.var_type().getText();
			String name = ctx.id().IDENTIFIER().getText();
			this.current_table.addSymbol(name, new SymbolAttributes(type, ""));
		}

		@Override 
		public void exitParam_decl(LittleParser.Param_declContext ctx) { 
		
		
		}

		
		// Function Declarations__________________________________________________
		@Override 
		public void enterFunc_decl(LittleParser.Func_declContext ctx) {
			
			// this.current_table.addSymbol(ctx.id().IDENTIFIER().getText(), new SymbolAttributes("FUNCTION");
			
			this.symbol_table_stack.push(new SymbolTable(ctx.id().IDENTIFIER().getText()));
			this.current_table = this.symbol_table_stack.peek();
	
		
		}

		@Override 
		public void exitFunc_decl(LittleParser.Func_declContext ctx) { 
			
		
		}
	
		@Override public void enterWhile_stmt(LittleParser.While_stmtContext ctx) {
			block_count++;
			this.symbol_table_stack.push(new SymbolTable("BLOCK " + block_count));
			this.current_table = this.symbol_table_stack.peek();
		 }

		@Override public void exitWhile_stmt(LittleParser.While_stmtContext ctx) { 
			
			// Pop from ST stack and push onto print stack for printing
			
		}

		@Override public void enterIf_stmt(LittleParser.If_stmtContext ctx) {
			
			block_count++;
			
			this.symbol_table_stack.push(new SymbolTable("BLOCK " + block_count));
			this.current_table = this.symbol_table_stack.peek();
		 }

		@Override public void exitIf_stmt(LittleParser.If_stmtContext ctx) { 
			
		}

		@Override public void enterElse_part(LittleParser.Else_partContext ctx) {
			
			if(ctx.getText() != ""){
			
				block_count++;
			
				this.symbol_table_stack.push(new SymbolTable("BLOCK " + block_count));
				this.current_table = this.symbol_table_stack.peek();
			}
			
		 }

		@Override public void exitElse_part(LittleParser.Else_partContext ctx) {
		
		 }
		
		//------------------------------------------------------------------------------------/Proj 4
		
		@Override 
		public void enterAssign_expr(LittleParser.Assign_exprContext ctx) { 
			
			//id := expr
			
			String expression = ctx.getText();
			// System.out.print("\n\nEXPR: " + expression);

			

			//System.out.print("\n\nAssign_to: ");
			//System.out.print(ctx.id().IDENTIFIER().getText());

			String[] elements = expression.split(":="); //Split at equal sign to get both sides -> a = b + c => ['a','b+c']
			String assign_to = ctx.id().IDENTIFIER().getText(); //First index should hold variable that the exp is assigned to

			
			try{
				int num = Integer.parseInt(elements[1]);

				Node<String> root = new Node<> (":=");
				Node<String> theType = root.addChild(new Node<String>("INT"));
				Node<String> assigned = theType.addChild(new Node<String>(assign_to));
				Node<String> val = assigned.addChild(new Node<String>(elements[1]));

				ASTs.add(root);
			}
			catch(Exception e){
				try{
					float fnum = Float.parseFloat(elements[1]);

					Node<String> root = new Node<> (":=");
					Node<String> theType = root.addChild(new Node<String>("FLOAT"));
					Node<String> assigned = theType.addChild(new Node<String>(assign_to));
					Node<String> val = assigned.addChild(new Node<String>(elements[1]));
				
					ASTs.add(root);
				}
				catch(Exception f){
					String operation;
					String[] operands;
					
					// Addition
					if(elements[1].contains("+")) {
						
						operation = "+";
						operands = elements[1].split("\\+"); 
					}

					//Subtraction
					else if (elements[1].contains("-")){
						operation = "-";
						operands = elements[1].split("-");
					}

					//Multiplication
					else if(elements[1].contains("*")) {
						operation = "*";
						operands = elements[1].split("\\*");
					}

					//Division
					else{
						operation = "/";
						operands = elements[1].split("/");
					}
						
					

					String op1 = operands[0];
					String op2 = operands[1];
					Stack<SymbolTable> temp = new Stack<SymbolTable>();
					while(current_table.findEntry(assign_to) == null){
						temp.push(symbol_table_stack.pop());
						current_table = symbol_table_stack.peek();
					}
					String get_the_type = current_table.findEntry(assign_to).gethetype();
					for(int stack = 0; stack < temp.size(); stack++){
						symbol_table_stack.push(temp.pop());
					}
					current_table = symbol_table_stack.peek();

					Node<String> root = new Node<> (":=");
					Node<String> theType = root.addChild(new Node<String>(get_the_type));
					Node<String> opsymbol = theType.addChild(new Node<String>(operation));
					Node<String> assigned = opsymbol.addChild(new Node<String>(assign_to));
					Node<String> operation2 = assigned.addChild(new Node<String>(op2));
					Node<String> operation1 = operation2.addChild(new Node<String>(op1));
					//divi opmrl reg         ; computes reg = reg /  op1
					// y := z/y;				;DIVF z y $T6		divr y r6	move r6 y
					ASTs.add(root);
				}
			}		
		}
		
		@Override public void exitAssign_expr(LittleParser.Assign_exprContext ctx) { }
		
		@Override public void enterRead_stmt(LittleParser.Read_stmtContext ctx) {
			
			Stack<SymbolTable> temp = new Stack<SymbolTable>();

			String id_list = ctx.id_list().getText(); // READ(a, b) => id_list = 'a, b';

			//System.out.println("\n\nidlist: "+id_list);

			String[] ids = id_list.split(",");
			for(int id = 0; id < ids.length; id++){
				//for (int stack = 0; stack < symbol_table_stack.size(); stack++){

					while(current_table.findEntry(ids[id]) == null){
						temp.push(symbol_table_stack.pop());
						current_table = symbol_table_stack.peek();
					}
					String type = current_table.findEntry(ids[id]).gethetype();
					if(type.contains("STRING")){
						String value = current_table.findEntry(ids[id]).getValue();
						
						Node<String> root_read = new Node<> ("READ");

						Node<String> type_of_root = new Node<>(type);
						root_read.addChild(type_of_root);

						Node<String> id_of_root = new Node<>(ids[id]);
						type_of_root.addChild(id_of_root);
						
						ASTs.add(root_read);	
					}
					else{
						Node<String> root_read = new Node<> ("READ");

						Node<String> type_of_root = new Node<>(type);
						root_read.addChild(type_of_root);

						Node<String> id_of_root = new Node<>(ids[id]);
						type_of_root.addChild(id_of_root);

						ASTs.add(root_read);
					}
			}
			for(int stack = 0; stack < temp.size(); stack++){
				symbol_table_stack.push(temp.pop());
			}
			current_table = symbol_table_stack.peek();
		

		}
		@Override public void exitRead_stmt(LittleParser.Read_stmtContext ctx) { }
		
		@Override public void enterWrite_stmt(LittleParser.Write_stmtContext ctx) { 

			Stack<SymbolTable> temp = new Stack<SymbolTable>();

			String id_list = ctx.id_list().getText(); 

			String[] ids = id_list.split(",");

			for(int id = 0; id < ids.length; id++){

				while(current_table.findEntry(ids[id]) == null){
					temp.push(symbol_table_stack.pop());
					current_table = symbol_table_stack.peek();
				}
				String type = current_table.findEntry(ids[id]).gethetype();
				if(type.contains("STRING")){
					String value = current_table.findEntry(ids[id]).getValue();
					
					Node<String> root_write = new Node<> ("WRITE");

					Node<String> type_of_root = new Node<>(type);
					root_write.addChild(type_of_root);

					Node<String> id_of_root = new Node<>(ids[id]);
					type_of_root.addChild(id_of_root);

					ASTs.add(root_write);
				}
				else{
					Node<String> root_write = new Node<> ("WRITE");

					Node<String> type_of_root = new Node<>(type);
					root_write.addChild(type_of_root);
					
					Node<String> id_of_root = new Node<>(ids[id]);
					type_of_root.addChild(id_of_root);

					ASTs.add(root_write);
				}
				for(int stack = 0; stack < temp.size(); stack++){
					symbol_table_stack.push(temp.pop());
				}
				current_table = symbol_table_stack.peek();
				
			}
		
		}
		
		@Override public void exitWrite_stmt(LittleParser.Write_stmtContext ctx) { }
		
	}
	
	static class SymbolTable {
		
		private String scope;
		
		private HashMap<String, SymbolAttributes> symbolTable;
		
		private ArrayList<String> symbolNames;
		
		public SymbolTable(String scope) {
			this.scope = scope;
			this.symbolTable = new HashMap<String, SymbolAttributes>();
			this.symbolNames = new ArrayList<String>(); 
		}
		
		public SymbolAttributes findEntry(String entry){

			return this.symbolTable.get(entry);
		}

		public String getScope() {
			return this.scope;
		}
		
		public void addSymbol(String name, SymbolAttributes attr) {
			
			if(this.symbolTable.containsKey(name)) {
				
				System.out.printf("DECLARATION ERROR %s\n", name);
				System.exit(0);
				
			}
			
			this.symbolTable.put(name, attr);
			this.symbolNames.add(name);
		}
		
	}
	
	
	static class SymbolAttributes {
		String type;
		String value;
		
		public SymbolAttributes (String type, String value) {
			this.type = type;
			this.value = value;
		}
		
		public String gethetype() {
			return this.type;
		}
		
		public String getValue() {
			return this.value;
		}
	}
	
	
	static class Node<T> {
		 
		private T data = null;
		 
		private Node<T> child = null;
		 
		private Node<T> parent = null;
		 
		public Node(T data) {
		this.data = data;
		}
		 
		public Node<T> addChild(Node<T> child) {
		child.setParent(this);
		this.setChild(child);
		return child;
		}
		 
		public Node<T> getChild() {
		return child;
		}
		 
		public T getData() {
		return data;
		}
		 
		public void setData(T data) {
		this.data = data;
		}
		 
		private void setParent(Node<T> parent) {
		this.parent = parent;
		}
		
		private void setChild(Node<T> child) {
			this.child = child;
			}

		public Node<T> getParent() {
		return parent;
		}
		
		 
	}
	
	private static <T> void printTree(Node<T> node, String appender) {
	  
		System.out.println(appender + node.getData());
		printTree(node.getChild(), appender + appender);
	}
		
	
	public static class VerboseListener extends BaseErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer,
			Object offendingSymbol,
			int line, int charPositionInLine,
			String msg,
			RecognitionException e)
		{
			List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
			
			Collections.reverse(stack);
			
			if(stack.size() > 0) {
				// System.out.System.out.println("\nNot accepted\n");
				System.exit(1);
			}
			
		}
	}
}
