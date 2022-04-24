
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
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
		
		//System.out.System.out.println(tree.toStringTree(parser));
		
		//parser.removeErrorListeners();
		
		//parser.addErrorListener(new VerboseListener());
		
		//parser.program();
		
		//System.out.System.out.println("\nAccepted\n");
		
	}
	
	static class SymbolExtractor extends LittleBaseListener {
		
		private Stack<SymbolTable> symbol_table_stack;
		private Stack<SymbolTable> symbol_table_stack_seen;
		private SymbolTable current_table;
		private int block_count;
		
		public SymbolExtractor() {
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
		
			//System.out.println("\n\n\n" + this.symbol_table_stack_seen.size() + "\n\n\n");
			
			//this.symbol_table_stack_seen.push(this.symbol_table_stack.pop());
			
			int size_stack = symbol_table_stack.size();
			
			for(int pop = 0; pop < size_stack; pop++){
			
				this.symbol_table_stack_seen.push(this.symbol_table_stack.pop());
				
			}
			
			/*
			
				|GLOBAL|main| 2
				
				current = GLOBAL 
				
				prints all of array list for GLOBAL
				
				stack_iterate = 1
				
				current = main
				
				
			
			*/
			
			// For loop to iterate through print stack
			
			int size = symbol_table_stack_seen.size();
			
			for (int stack_iterate = 0; stack_iterate < size; stack_iterate++) {
				
				// System.out.println("\n\nSTACK SIZE: " + symbol_table_stack_seen.size() + "\n\n");
				// System.out.println("\n\nSTACK ITERATE VALUE: " + stack_iterate + "\n\n");
				
				// Pop off stack to get current symbol table to work with
				SymbolTable current = symbol_table_stack_seen.pop();
				
				//Print current scope
				System.out.println("\nSymbol table " + current.getScope());
				
				// Iterate through arraylist to print symbols in current symbol table
				for(int array_iterate = 0; array_iterate < current.symbolNames.size(); array_iterate++) {
					
					String symbol = current.symbolNames.get(array_iterate);
					
					SymbolAttributes value = current.symbolTable.get(symbol);
					
					// If the type is a string, print name, type and value 
					// Else print name and type for other symbols
					if(value.getType() == "STRING") {
						System.out.println("name " + symbol + " type " + value.getType() + " value " + value.getValue());
					}
					
					else {
						
						System.out.println("name " + symbol + " type " + value.getType());
					
					}
					
				}
				
			}
			
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
		
		@Override public void enterParam_decl_list(LittleParser.Param_decl_listContext ctx) { 
		/*
			String decl_list_str = ctx.getText();
			
			if(decl_list_str != ""){
			
				System.out.print("\n\nBEFORE: " + decl_list_str + "\n\n");
				String[] decl_list = decl_list_str.split(",");
				System.out.print("\n\nAFTER: " + decl_list + "\n\n");
			
				for(int j = 0; j < decl_list.length; j++) {
				
					String[] temp = decl_list[j].split(" ");
					this.current_table.addSymbol(temp[1], new SymbolAttributes(temp[0], ""));
				
				}
			
			}
		*/
		}
	
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
			
			//System.out.print("\n\nENTER IF. BLOCK " + block_count + "\n\n");
			
			this.symbol_table_stack.push(new SymbolTable("BLOCK " + block_count));
			this.current_table = this.symbol_table_stack.peek();
		 }

		@Override public void exitIf_stmt(LittleParser.If_stmtContext ctx) { 
			
		}

		@Override public void enterElse_part(LittleParser.Else_partContext ctx) {
			
			if(ctx.getText() != ""){
			
				block_count++;
			
				//System.out.print("\n\nENTER ELSE. BLOCK " + block_count + "\n\n");
			
				this.symbol_table_stack.push(new SymbolTable("BLOCK " + block_count));
				this.current_table = this.symbol_table_stack.peek();
			}
			
		 }

		@Override public void exitElse_part(LittleParser.Else_partContext ctx) {
		
		 }
		
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
			//this.symbolTable.get(name).getValue().getType()
		}
		
	}
	
	
	static class SymbolAttributes {
		String type;
		String value;
		
		public SymbolAttributes (String type, String value) {
			this.type = type;
			this.value = value;
		}
		
		public String getType() {
			return this.type;
		}
		
		public String getValue() {
			return this.value;
		}
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
			/*
			System.err.System.out.println("rule stack: "+stack);
			System.err.System.out.println("line "+line+":"+charPositionInLine+" at "+
			offendingSymbol+": "+msg);
			*/
		}
	}
}
