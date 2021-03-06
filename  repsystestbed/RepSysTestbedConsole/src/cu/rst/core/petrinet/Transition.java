package cu.rst.core.petrinet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cu.rst.core.alg.Algorithm;
import cu.rst.core.graphs.Feedback;
import cu.rst.core.graphs.FHG;
import cu.rst.core.graphs.RG;
import cu.rst.core.graphs.ReputationEdge;
import cu.rst.core.graphs.TG;
import cu.rst.core.graphs.TrustEdge;
import cu.rst.util.Util;

/**
 * A transition embeds an algorithm and a transition is triggered only when there are enough tokens in the 
 * input places. Logic to determine if there are enough tokens is as follows:
 * <br> total required tokens is the sum of weights on all incoming edges of t
 * <br> total available tokens is the sum of the tokens in the input places of t
 * if(total available tokens >= total required tokens) then the transition is fired.
 * Once the transition is fired, number of tokens deleted in each input place =
 * weight of the edge leading to it.
 * 
 *  
 * @author partheinstein
 *
 */
public class Transition implements PetriNetElementIntf
{
	static Logger logger = Logger.getLogger(Transition.class.getName());
	
	protected int m_id;
	private PetriNet m_net; 
	private Algorithm m_alg;
	
	public Transition()
	{
		m_id = PetriNet.globalCounter++;
	}
	
	public Transition(Algorithm alg)
	{
		
		m_id = PetriNet.globalCounter++;
		try
		{
			setAlgorithm(alg);
		}
		catch(Exception e)
		{
			logger.error(e);
		}
	}
	
	public void setAlgorithm(Algorithm alg) throws Exception
	{
		Util.assertNotNull(alg);
		m_alg = alg;
		m_alg.setTransition(this);
	}
	
	public Algorithm getAlg()
	{
		Util.assertNotNull(m_alg);
		return m_alg;
	}
	
	@Override
	public void setWorkflow(PetriNet net) throws Exception
	{
		Util.assertNotNull(net);
		m_net = net;
		if(m_alg != null) m_alg.setWorkflow(net);
	}
	
	public PetriNet getWorkflow()
	{
		return m_net;
	}

	/**
	 * This method returns whether there are sufficient tokens in the input places of this transition. This
	 * method is used to determine whether to fire this transition or not.
	 * @param tokens
	 * @return
	 * @throws Exception
	 */
	public boolean canFire() throws Exception
	{
		logger.debug("canFire() invoked for " + this.m_alg.getName());
		Util.assertNotNull(m_net);
		
		int totalTokensInInputPlaces = 0;
		int totalTokensRequired = 0;
		
		Set<PetriNetEdge> incomingEdges = m_net.incomingEdgesOf(this);
		if(incomingEdges != null && incomingEdges.size() > 0)
		{
			logger.debug("Number of input places: " + incomingEdges.size());
			for(PetriNetEdge e : incomingEdges)
			{
				logger.debug("Incoming edge:" + e);
				//determine the tokens we have so far from the input places
				if(e.src instanceof Place)
				{
					logger.debug("Getting tokens from " + ((Place)e.src));
					totalTokensInInputPlaces = totalTokensInInputPlaces + ((Place)e.src).numTokens(); 
					//totalTokensRequired = totalTokensRequired + e.getTokens();
					if(totalTokensInInputPlaces < e.getTokens()) return false;
				}
				else
				{
					throw new Exception("Source to the edge is expected to be a place.");
				}
			}
			
//			Set<PetriNetEdge> outgoingEdges = m_net.outgoingEdgesOf(this);
//			if(outgoingEdges!=null && outgoingEdges.size()>0)
//			{
//				logger.debug("Number of output places: " + outgoingEdges.size());
//				for(PetriNetEdge e : outgoingEdges)
//				{
//					if(e.sink instanceof Place)
//					{
//						totalTokensRequired = totalTokensRequired + e.getTokens();
//					}
//				}
//				logger.debug("Total tokens required: " + totalTokensRequired);
//			}
			
			
			
//			logger.debug("Number of tokens in the input places: " + totalTokensInInputPlaces + ", Number of required tokens: " + totalTokensRequired);
			//do we have enough of the tokens to match the tokens on the edges?
			if(totalTokensInInputPlaces < totalTokensRequired) return false;	
		}
		else
		{
			logger.debug("Transition " + this.m_alg.getName() + "cannot fire.");
			return false;
		}
		logger.debug("Transition " + this.m_alg.getName() + " can fire.");
		return true;
	}
	

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Transition)
		{
			Transition t = (Transition)o;
			return (t.m_id == this.m_id)? true : false;
		}
		else return false;
	}
	
	/**
	 * Fires the transition.
	 * 
	 * This method will fire only if it can. If it can't, its a no-op.
	 * If it fires, tokens are added to the outgoing places and removed 
	 * from the incoming places according to the incoming edge weight.
	 * @throws Exception
	 */
	@Deprecated
	public boolean fire() throws Exception
	{
		logger.debug("fire() invoked.");
		Util.assertNotNull(m_net);
		
		//can this transition fire?
		if(canFire())
		{
			//yes, get the tokens from all the incoming places of this transition
			ArrayList allTokens = new ArrayList();
			Set<PetriNetEdge> incomingEdges = m_net.incomingEdgesOf(this);
			if(incomingEdges != null && incomingEdges.size() > 0)
			{
				for(PetriNetEdge e : incomingEdges)
				{
					if(e.src instanceof Place)
					{
						//get only the required amount tokens from the input place.
						List<Token> toks = ((Place)e.src).getTokens(e.getTokens());
						if(toks!=null)
						{
							for(Token t : toks)
							{
								allTokens.add(t);
							}
						}
						
					}
				}
				
				logger.debug("Total tokens in all the input places: " + allTokens.size());
				logger.debug("Calling algorithm.update()");
				
				//call the update function and pass the tokens in the incoming places
				//get the changes
				ArrayList newChanges = this.update(allTokens);
				
				logger.debug("The algorithm returns " + newChanges.size() + " changes.");
				
				logger.debug("Deleting the tokens from the input places according to the label on the edges.");
				//delete the tokens from the input places according to the label on the edges
				for(PetriNetEdge e : incomingEdges)
				{
					if(e.src instanceof Place)
					{
						((Place)e.src).deleteTokens(e.getTokens());
						logger.debug("Deleted " + e.getTokens() + " tokens. Remaining tokens in " + ((Place)e.src));
					}
				}
				
				//add the tokens to the outgoing places according to the label on the edges
				Set<PetriNetEdge> outgoingEdges = m_net.outgoingEdgesOf(this);
				if(outgoingEdges != null && outgoingEdges.size() > 0)
				{
					logger.debug("Number of outgoing places: " + outgoingEdges.size());
					for(PetriNetEdge e : outgoingEdges)
					{
						if(e.sink instanceof Place)
						{
							//uncomment the following if support for edge weights are needed
							//int numTokensToAdd = e.getTokens();
							int numTokensToAdd = 1; 
							for(int i=0; i<numTokensToAdd; i++)
							{
								Place p = ((Place)e.sink);
								Token t = new Token(newChanges, p);
								p.putToken(t, true);
								
								//set putToken(token, false) if fire() is invoked from workflow.traverse() method.
								//because update() method is invoked in workflow.traverse()
								logger.debug("Added " + numTokensToAdd + " to " + ((Place)e.sink));
							}
							
							
							
						}
						else
						{
							throw new Exception("Expected a place."); 
						}
					}
				}
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * Fires the transition.
	 * This method will fire only if it can. If it can't, its a no-op.
	 * If it fires, tokens are added to the outgoing places and removed 
	 * from the incoming places according to the incoming edge weight.
	 * <br>
	 * This method differs from fire() because it invokes update(ArrayList, Place)
	 * @see {@link cu.rst.core.petrinet.PetriNetElementIntf#update(ArrayList, Place)}
	 * @throws Exception
	 */
	public boolean fire2() throws Exception
	{
		logger.debug("fire() invoked.");
		Util.assertNotNull(m_net);
		
		//can this transition fire?
		if(canFire())
		{
			//yes, get the tokens from all the incoming places of this transition
			ArrayList allTokens = new ArrayList();
			Set<PetriNetEdge> incomingEdges = m_net.incomingEdgesOf(this);
			if(incomingEdges != null && incomingEdges.size() > 0)
			{
				for(PetriNetEdge e : incomingEdges)
				{
					if(e.src instanceof Place)
					{
						//get only the required amount tokens from the input place.
						List<Token> toks = ((Place)e.src).getTokens(e.getTokens());
						if(toks!=null)
						{
							for(Token t : toks)
							{
								allTokens.add(t);
							}
						}
						
					}
				}
				
				logger.debug("Total tokens in all the input places: " + allTokens.size());
				
				//add the tokens to the outgoing places according to the label on the edges
				Set<PetriNetEdge> outgoingEdges = m_net.outgoingEdgesOf(this);
				if(outgoingEdges != null && outgoingEdges.size() > 0)
				{
					logger.debug("Number of outgoing places: " + outgoingEdges.size());
					for(PetriNetEdge e : outgoingEdges)
					{
						if(e.sink instanceof Place)
						{
							//call the update function and pass the tokens in the incoming places
							//get the changes
							logger.debug("Calling algorithm.update()");
							ArrayList newChanges = this.update(allTokens, (Place)e.sink);
							logger.debug("The algorithm returns " + newChanges.size() + " changes.");
							
							//uncomment the following if support for edge weights are needed
							//int numTokensToAdd = e.getTokens();
							int numTokensToAdd = 1; 
							for(int i=0; i<numTokensToAdd; i++)
							{
								Place p = ((Place)e.sink);
								Token t = new Token(newChanges, p);
								p.putToken(t, true);
								
								//set putToken(token, false) if fire() is invoked from workflow.traverse() method.
								//because update() method is invoked in workflow.traverse()
								logger.debug("Added " + numTokensToAdd + " to " + ((Place)e.sink));
							}
							
							
							
						}
						else
						{
							throw new Exception("Expected a place."); 
						}
					}
				}
				

				logger.debug("Deleting the tokens from the input places according to the label on the edges.");
				//delete the tokens from the input places according to the label on the edges
				for(PetriNetEdge e : incomingEdges)
				{
					if(e.src instanceof Place)
					{
						((Place)e.src).deleteTokens(e.getTokens());
						logger.debug("Deleted " + e.getTokens() + " tokens. Remaining tokens in " + ((Place)e.src));
					}
				}
				
			}
			
			return true;
		}
		return false;
	}

	private ArrayList<TrustEdge> getTrustEdges(ArrayList newChanges) 
	{
		ArrayList<TrustEdge> edges = new ArrayList<TrustEdge>();
		for(Object o : newChanges)
		{
			if(o instanceof TrustEdge) edges.add((TrustEdge)o);
		}
		return edges;
	
	}

	private ArrayList<ReputationEdge> getRepEdges(ArrayList newChanges) 
	{
		ArrayList<ReputationEdge> edges = new ArrayList<ReputationEdge>();
		for(Object o : newChanges)
		{
			if(o instanceof ReputationEdge) edges.add((ReputationEdge)o);
		}
		return edges;
	}

	private ArrayList<Feedback> getFeedbacks(ArrayList newChanges) 
	{
		ArrayList<Feedback> feedbacks = new ArrayList<Feedback>();
		for(Object o : newChanges)
		{
			if(o instanceof Feedback) feedbacks.add((Feedback)o);
		}
		return feedbacks;
	}

	@Override
	public ArrayList update(ArrayList<Token> tokens) throws Exception 
	{
		ArrayList changes = m_alg.update(tokens);
		if(changes == null) changes = new ArrayList();
		return changes;
	}
	
	@Override
	public String toString()
	{
		String name = (m_alg!=null)? m_alg.getClass().getCanonicalName() : null;
		return "Transition: " + name;
	}
	
	@Override
	public String getName()
	{
		return (m_alg!=null)? m_alg.getClass().getSimpleName() + "_" + m_id : null;
	}
	
	public ArrayList update(ArrayList<Token> tokens, Place p) throws Exception
	{
		ArrayList changes = m_alg.update(tokens, p);
		if(changes == null) changes = new ArrayList();
		return changes;
	}
}
