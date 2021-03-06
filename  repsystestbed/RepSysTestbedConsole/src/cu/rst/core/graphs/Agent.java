package cu.rst.core.graphs;

import org.apache.log4j.Logger;


public class Agent 
{
	public static int agentCounter=0;
	static Logger logger = Logger.getLogger(Agent.class.getName());
	
	public final int id;
//	private ReputationAlgorithm repAlg;
//	private TrustAlgorithm trustAlg;
//	private FeedbackHistoryGraph feedbackHistoryGraph;
//	private ReputationGraph reputationGraph;
//	private TrustGraph trustGraph;
	
	public Agent()
	{
		id=agentCounter;
		agentCounter++;
	}
	
	public Agent(int id)
	{
		this.id = id;
	}
	
	public String toString()
	{
		return "Agent " + id + " ";
	}
	
	@Override
	public boolean equals(Object o)
	{
		Agent otherAgent = (Agent) o;
		if(this.id==otherAgent.id) return true;
		else return false;
	}
	
	@Override
	public int hashCode()
	{
		return this.id;
	}
/*
	public void setReputationAlg(ReputationAlgorithm repAlg)
	{
		this.repAlg = repAlg;
	}

	public ReputationAlgorithm getReputationAlg()
	{
		return repAlg;
	}

	public void setTrustAlg(TrustAlgorithm trustAlg)
	{
		this.trustAlg = trustAlg;
	}

	public TrustAlgorithm getTrustAlg()
	{
		return trustAlg;
	}
	
	public void updateGraphs()
	{
		
		
	}
*/	
}