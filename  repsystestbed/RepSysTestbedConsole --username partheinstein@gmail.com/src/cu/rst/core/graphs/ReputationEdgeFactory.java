package cu.rst.core.graphs;

import org.jgrapht.EdgeFactory;

public class ReputationEdgeFactory implements EdgeFactory<Agent, ReputationEdge>
{

	@Override
	public ReputationEdge createEdge(Agent src, Agent sink)
	{
		
		return new ReputationEdge(src, sink);
	}

}
