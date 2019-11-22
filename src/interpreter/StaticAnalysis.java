package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import interpreter.StaticSemanticException;
//import cop5556fa19.BuildSymbolTable;
import cop5556fa19.Parser;
import cop5556fa19.Scanner;
import cop5556fa19.Token;
import cop5556fa19.Token.Kind;
import cop5556fa19.AST.*;
import interpreter.ASTVisitorAdapter.TypeException;
import interpreter.built_ins.print;
import interpreter.built_ins.println;
import interpreter.built_ins.toNumber;

public class StaticAnalysis extends ASTVisitorAdapter{
	LuaTable _ST; //global environment
	int  current_scope = 0, next_scope = 1;
	Stack<Integer> scope_stack = new Stack<>();
	Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
	Map<String, List<StatLabel>> mapLabel = new HashMap<>();
	void init_ST() {
		_ST = new LuaTable();
		
	}
	
	ASTNode root; //useful for debugging
	
	public StaticAnalysis() {
		init_ST();
	}
	

	public void enterScope() {
		scope_stack.push(current_scope);
		current_scope = next_scope++; 
	}

	public void closeScope() {
		current_scope = scope_stack.pop();
		next_scope = current_scope + 1;
	}

	public int getCurrentScope() {
		return this.current_scope;
	}
	
	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		Block b = chunk.block;
		List<LuaValue> result = (List<LuaValue>) b.visit(this,_ST);
		assignGoto(b, arg);
		return result;
	}
	
	public Object assignGoto(Block block, Object arg) throws Exception {
		List<Stat> list = block.stats;
		List<LuaValue> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Stat element = list.get(i);
			if(element instanceof StatGoto) {
				Name n = ((StatGoto) element).name;
				Token t = element.firstToken;
				if (map.containsKey(n.name)) {
					List<Integer> r = map.get(n.name);
					List<StatLabel> st = mapLabel.get(n.name);
					while (r.size() > 0 && r.get(0) > current_scope) {
						r.remove(0);
						st.remove(0);
					}
					map.put(n.name, r);
					mapLabel.put(n.name, st);
				}else {
					throw new interpreter.StaticSemanticException(element.firstToken, "assignGoto");
				}
				((StatGoto) element).label = mapLabel.get(n.name).get(0);
			}else if (element instanceof StatIf) {
				List<Block> bs = ((StatIf) element).bs;
				for (Block b : bs) {
					enterScope();
					assignGoto(b, arg);
				}
			}else if (element instanceof StatRepeat) {
				Block b = ((StatRepeat) element).b;
				enterScope();
				assignGoto(b, arg);
			}else if (element instanceof StatWhile) {
				Block b = ((StatWhile) element).b;
				enterScope();
				assignGoto(b, arg);
			}else if (element instanceof StatDo) {
				Block b = ((StatDo) element).b;
				enterScope();
				assignGoto(b, arg);
			}
			else {
				//throw new TypeException(block.firstToken, "StaticAnalysisVisitBlockTypeError");
			}
		}
		if (scope_stack.size() != 0) {
			closeScope();
		}
		
		return result;
	}
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		List<Stat> list = block.stats;
		List<LuaValue> result = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Stat element = list.get(i);
			if(element instanceof StatLabel) {
				Name n = ((StatLabel) element).label;
				Token t = element.firstToken;
				StatLabel addS = new StatLabel(t, n, block, i);
				block.stats.set(i, addS);
				if (map.containsKey(n.name)) {
					List<Integer> temp = map.get(n.name);
					List<StatLabel> tempLabel = mapLabel.get(n.name);
					temp.add(0, current_scope);
					tempLabel.add(addS);
					map.put(n.name, temp);
					mapLabel.put(n.name, tempLabel);
				}else {
					List<Integer> temp = new ArrayList<Integer>();
					temp.add(current_scope);
					List<StatLabel> tempLabel = new ArrayList<>();
					tempLabel.add(addS);
					map.put(n.name, temp);
					mapLabel.put(n.name, tempLabel);
				}
			}else if (element instanceof StatIf) {
				List<Block> bs = ((StatIf) element).bs;
				for (Block b : bs) {
					enterScope();
					b.visit(this, _ST);
				}
			}else if (element instanceof StatRepeat) {
				Block b = ((StatRepeat) element).b;
				enterScope();
				b.visit(this, _ST);
			}else if (element instanceof StatWhile) {
				Block b = ((StatWhile) element).b;
				enterScope();
				b.visit(this, _ST);
			}else if (element instanceof StatDo) {
				Block b = ((StatDo) element).b;
				enterScope();
				b.visit(this, _ST);
			}
			else {
				//throw new TypeException(block.firstToken, "StaticAnalysisVisitBlockTypeError");
			}
		}
		if (scope_stack.size() != 0) {
			closeScope();
		}
		return result;
	}

	@Override
	public List<LuaValue> load(Reader r) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
