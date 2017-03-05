import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

/**
 * 27-2-2017
 **/
class Player
{

	static class Index implements Comparable<Index>
	{
		int priority;
		int index;

		public Index(int p, int i)
		{
			priority = p;
			index = i;
		}

		@Override
		public int compareTo(Index o)
		{

			return this.priority - o.priority;

		}

	}

	static final int PROTECT = 0;
	static final int ATTACK = 1;
	static final int CLAIM = 2;
	static final int UPGRADE = 3;

	static class Possibility implements Comparable<Possibility>
	{

		int timeUsed;
		ArrayList<Factory> usedFactories;

		@Override
		public int compareTo(Possibility o)
		{
			return this.timeUsed - o.timeUsed;
		}
	}

	static class Action implements Comparable<Action>
	{
		double cost;
		Factory target;
		double reward;
		int type;

		@Override
		public int compareTo(Action o)
		{
			return (int) (Math.round((o.cost / o.reward) - (this.cost / this.reward)));
		}

	}

	static class Factory
	{
		public double flow;
		boolean isDeactive;
		int production;
		int troops;
		int usableTroops;
		int base = 0;
		int index;
		int actualTroops;
	}

	static class Troop
	{
		boolean mine;
		int size;
		int distanceLeft;
		int fromFact;
		int toFact;
	}

	static point[][] graph;
	static int[] previousConnections; // path?
	static int[][] fromToNext;
	static int usedBombs = 0;
	static Factory[] factories;
	static int[][] distances;
	static int highestProductivity;
	static int factoryCount;
	static Scanner in;

	public static void main()
	{

		/*
		 * INIT
		 */
		in = new Scanner(System.in);

		factoryCount = in.nextInt(); // the number of factories

		// small number of factories, large number of grids
		// matrix is more efficient
		distances = new int[factoryCount][factoryCount];

		int linkCount = in.nextInt(); // the number of links between factories
		for (int i = 0; i < linkCount; i++)
		{
			int factory1 = in.nextInt();
			int factory2 = in.nextInt();
			int distance = in.nextInt();
			distances[factory1][factory2] = distance;
			distances[factory2][factory1] = distance;
		}

		highestProductivity = 0;
		factories = new Factory[factoryCount];
		for (int i = 0; i < factories.length; i++)
		{
			factories[i] = new Factory();
			factories[i].index = i;
		}

		fromToNext = new int[distances.length][distances.length];

		/*
		 * GAME LOOP
		 */
		playGame();

	}

	private static void playGame()
	{

		boolean firstTurn = true;
		int turnCount = 0;
    int highestProd = 0;
    
		graph = new point[factoryCount][];
		for (int i = 0; i < factoryCount; i++)
		{
			graph[i] = dijkstra(distances, i);
		}

		while (true)
		{

			turnCount++;

			/*
			 * 
			 * 
			 * COLLECT DATA
			 * 
			 * 
			 */
			int[] bomb = new int[factoryCount];
			int entityCount = in.nextInt(); // the number of entities (e.g.
											// factories and troops)

			List<Troop> troops = new ArrayList<Troop>();
			Set<Integer> myFactories = new HashSet<Integer>();
			Set<Factory> myFactoryObjects = new HashSet<Factory>();
			Set<Integer> enemyFactories = new HashSet<Integer>();
			for (int i = 0; i < entityCount; i++)
			{
				int entityId = in.nextInt();
				String entityType = in.next();
				if (entityType.equals("FACTORY"))
				{
					int playerOwned = in.nextInt();
					int cyborgs = in.nextInt();
					int production = in.nextInt();
					factories[entityId].isDeactive = false;
					if (production > highestProductivity)
					{
						highestProductivity = production;
					}
					factories[entityId].troops = cyborgs;
					factories[entityId].usableTroops = cyborgs;

					if (factories[entityId].production < production)
					{
						factories[entityId].production = production;
					} else if (factories[entityId].production > production && !firstTurn)
					{
						factories[entityId].isDeactive = true;
					}
					
					if(production > highestProd)
					{
					 highestProd = production;   
					}

					if (playerOwned == 1)
					{
						if (myFactories.contains(entityId) == false)
						{
							myFactories.add(entityId);
							myFactoryObjects.add(factories[entityId]);
						}

						if (turnCount > 3)
						{
							if (factories[entityId].production < 3 && factories[entityId].flow < 10)
							{
								factories[entityId].flow += new Double(factories[entityId].production + 1) / 2;
								factories[entityId].usableTroops -= (int) factories[entityId].flow;
								// System.err.println("flow of " + entityId + "
								// is " + factories[entityId].flow);
							}
						}
					} else
					{
						if (myFactories.contains(entityId))
						{
							myFactories.remove(entityId);
							myFactoryObjects.remove(factories[entityId]);
						}
						if (playerOwned == -1)
						{
							enemyFactories.add(entityId);
						}
					}

					in.nextInt();
					in.nextInt();
				} else if (entityType.equals("BOMB"))
				{
					// do nothing with troops for now.
					int arg1 = in.nextInt();
					int arg2 = in.nextInt();
					int target = in.nextInt();
					int arg4 = in.nextInt();
					int arg5 = in.nextInt();

					if (target != -1)
					{
						bomb[target] = arg4;
					}
				} else if (entityType.equals("TROOP"))
				{
					// do nothing with troops for now.
					int owner = in.nextInt();
					int arg2 = in.nextInt();
					int target = in.nextInt();
					int size = in.nextInt();
					int distance = in.nextInt();

					Troop t = new Troop();
					t.mine = owner == 1;
					t.fromFact = arg2;
					t.distanceLeft = distance;
					t.toFact = target;
					t.size = size;
					troops.add(t);

					if (!t.mine && t.distanceLeft < 15)
					{
						factories[t.toFact].usableTroops -= t.size;
					}

				} else
				{
					// do nothing with troops for now.
					int arg1 = in.nextInt();
					int arg2 = in.nextInt();
					int arg3 = in.nextInt();
					int arg4 = in.nextInt();
					int arg5 = in.nextInt();
				}

			}

			/*
			 * 
			 * 
			 * LOGIC
			 * 
			 * 
			 */

			/*
			 * DECISIONS
			 */
			List<Action> actionPool = new ArrayList<Action>();

			// for each factory
			for (int i = 0; i < factoryCount; i++)
			{

				// one of mine. Actions:
				if (myFactories.contains(i))
				{

					/*
					 * DEFENSE
					 */
					PriorityQueue<Index> timeLine = new PriorityQueue<Index>();
					for (Troop t : troops)
					{
						if (t.toFact == i)
						{
							if (t.mine)
							{
								Index index = new Index(t.distanceLeft, t.size);
								timeLine.add(index);
							} else
							{
								Index index = new Index(t.distanceLeft, -t.size);
								timeLine.add(index);
							}
						}
					}

					// will I be negative?
					int scenario = factories[i].troops;
					boolean isEnemy = false;
					int prevIndex = 0;
					boolean underAttack = false;
					// traverse timeline of attacks and defense
					while (!timeLine.isEmpty())
					{
						Index next = timeLine.remove();

						if (isEnemy)
						{
							scenario -= next.index;
						} else
						{
							scenario += next.index;

						}
						if (next.index < 0)
						{
							underAttack = true;
						}

						if (bomb[i] != 0)
						{
							if (bomb[i] >= prevIndex && bomb[i] < next.priority)
							{
								scenario -= 5 * factories[i].production;
								if (scenario < 0)
								{
									isEnemy = !isEnemy;
									scenario = Math.abs(scenario);
								}
							}
						}
						scenario += (next.priority - prevIndex) * factories[i].production;

						if (scenario < 0)
						{
							isEnemy = !isEnemy;
							scenario = Math.abs(scenario);
						}

						prevIndex = next.priority;
					}

					if (scenario < 3 || isEnemy)
					{

						factories[i].troops += (int) factories[i].flow;
						factories[i].flow = 0;
						// try to add Math.abs(scenario) before below0Index
						Action a = new Action();
						a.type = PROTECT;
						a.cost = Math.min(6, Math.abs(scenario));
						// * 2 because if you lose, the opponent gains
						// ----> difference of 2*production
						a.reward = factories[i].production;

						a.target = factories[i];
						actionPool.add(a);

					}

					// Upgrade
					if (factories[i].production < 3 && !underAttack)
					{

						Action a = new Action();
						a.type = UPGRADE;
						a.cost = 10;
						a.reward = 1;
						a.target = factories[i];
						actionPool.add(a);

					}

				} else if (enemyFactories.contains(i))
				{

					/*
					 * ATTACK
					 */

					// create timeline
					PriorityQueue<Index> timeLine = new PriorityQueue<Index>();
					for (Troop t : troops)
					{
						if (t.toFact == i)
						{
							if (t.mine)
							{
								Index index = new Index(t.distanceLeft, -t.size);
								timeLine.add(index);
							} else
							{
								Index index = new Index(t.distanceLeft, t.size);
								timeLine.add(index);
							}
						}
					}

					// determine cost
					int scenario = factories[i].troops;
					int prevIndex = 0;
					while (!timeLine.isEmpty())
					{
						Index next = timeLine.remove();
						if (next.index > 0)
						{
							scenario += next.index;

							if (bomb[i] != 0)
							{
								if (bomb[i] >= prevIndex && bomb[i] < next.priority)
								{
									scenario -= 5 * factories[i].production;
								}
							}
							scenario += (next.priority - prevIndex) * factories[i].production;
						}
						prevIndex = next.priority;
					}
					if (scenario >= -2)
					{
						Action a = new Action();
						a.type = ATTACK;
						a.cost = Math.min(4, scenario + 1);
						a.reward = factories[i].production;
						a.target = factories[i];
						actionPool.add(a);

					}
				} else
				{
					// unused factory
					// create timeline
					PriorityQueue<Index> timeLine = new PriorityQueue<Index>();
					for (Troop t : troops)
					{
						if (t.toFact == i)
						{
							if (t.mine)
							{
								Index index = new Index(t.distanceLeft, -t.size);
								timeLine.add(index);
							} else
							{
								Index index = new Index(t.distanceLeft, t.size);
								timeLine.add(index);
							}
						}
					}

					// determine cost
					int scenario = factories[i].troops;
					int prevIndex = 0;
					boolean willBeEvil = false;
					boolean willBeMine = false;
					while (!timeLine.isEmpty())
					{
						Index next = timeLine.remove();

						if (willBeEvil)
						{
							scenario += next.index;

							// add time to scenario
							scenario += (next.priority - prevIndex) * factories[i].production;

							if (scenario < 0)
							{
								willBeEvil = false;
								willBeMine = true;
							}
						} else if (willBeMine)
						{
							scenario -= next.index;

							// add time to scenario
							scenario += (next.priority - prevIndex) * factories[i].production;

							if (scenario < 0)
							{
								willBeEvil = true;
								willBeMine = false;
							}
						} else
						{
							if (next.index > 0)
							{
								scenario -= next.index;
								// if enemy gets it
								if (scenario < 0)
								{
									willBeEvil = true;
									willBeMine = false;
								}

							} else
							{
								scenario += next.index;
								// if I get it
								if (scenario < 0)
								{
									willBeEvil = false;
									willBeMine = true;
								}

							}
						}

						prevIndex = next.priority;
					}
					if (willBeMine)
					{
						// do nothing
					} else if (willBeEvil)
					{
						// Create attack
						Action a = new Action();
						a.type = ATTACK;
						a.cost = scenario + 1;
						a.reward = factories[i].production;
						a.target = factories[i];
						actionPool.add(a);

					} else
					{
						// create claim
						// Create attack
						Action a = new Action();
						a.type = CLAIM;
						a.cost = scenario + 1;
						a.reward = factories[i].production;
						a.target = factories[i];
						actionPool.add(a);

					}

				}
			}

			/*
			 * Collect Actions
			 */

			// inner class solve
			class Solve
			{
				int target;
				int from;
				int amount;
				boolean isUpgrade;
			}

			List<Solve> actions = new ArrayList<Solve>();

			int[] troopCount = new int[factoryCount];
			for (int i = 0; i < troopCount.length; i++)
			{
				troopCount[i] = factories[i].usableTroops;
			}

			for (int i : myFactories)
			{
				final int ind = i;
				List<Action> list = new ArrayList<Action>();

				// System.err.println(ind);
				// filter
				for (Action a : actionPool)
				{
					if ((a.type == UPGRADE && a.target.index == i) || a.target != factories[i])
					{

						list.add(a);
					}
				}

				if (factories[i].flow >= 10 && factories[i].troops >= 10 && factories[i].production < 3)
				{
					// force add an INC
					// System.err.println("ADD INC TO " + i);
					Action a = new Action();
					a.type = UPGRADE;
					a.cost = 10;
					a.reward = 2;
					a.target = factories[i];
					list.add(a);

				}

				// sort
				if (list.size() > 0)
				{
					PriorityQueue<Action> actionQueue = new PriorityQueue<Action>(list.size(), new Comparator<Action>()
						{

							@Override
							public int compare(Action o1, Action o2)
							{

								// prioritise closer actions to get best
								// payout

								double distance1 = graph[ind][o1.target.index].distance + 1;
								double cost1 = o1.cost;
								double reward1 = o1.reward;
								if (reward1 == 0)
								{
									cost1 *= 30;
								}

								if (o1.type == ATTACK)
								{
									if (!o1.target.isDeactive)
									{
										cost1 += (distance1 * (o1.target.production + 1));
									}
								}
								double result1 = (cost1 * 60 + (distance1 * 60)) / reward1;

								// v2

								double distance2 = graph[ind][o2.target.index].distance + 1;
								double cost2 = o2.cost;
								double reward2 = o2.reward;
								if (reward2 == 0)
								{
									cost2 *= 30;
								}

								if (o2.type == ATTACK)
								{
									if (!o2.target.isDeactive)
									{
										cost2 += (distance2 * (o2.target.production + 1));
									}
								}
								double result2 = (cost2 * 60 + (distance2 * 60)) / reward2;

								return Double.compare(result1, result2);
							}

						});
					actionQueue.addAll(list);

					while (!actionQueue.isEmpty() && troopCount[i] > 0)
					{

						Action next = actionQueue.remove();

						if (next.type == UPGRADE && next.target.index == i)
						{

							if (troopCount[i] >= 10 || factories[i].flow >= 10 && factories[i].production < 3)
							{
								Solve s = new Solve();
								s.from = i;
								s.target = i;
								s.amount = 10;
								s.isUpgrade = true;
								actionPool.remove(next);
								actions.add(s);

								troopCount[i] -= s.amount;

								factories[i].flow = 0;

								System.err.println("UPGRADE!! " + i + " " + next.target.index + " " + next.cost + " "
										+ next.reward);
								System.err.println(typeString[next.type]);

							} else
							{
								// because upgrading is the best action for you.
								// wait a turn.
								break;
							}
						} else
						{
							Solve s = new Solve();
							s.from = i;
							s.target = next.target.index;
							s.amount = (int) Math.min((int) Math.max(next.cost, 0), Math.max(troopCount[i], 0));

							// bomb protection
							if (bomb[fromToNext[s.from][s.target]] < graph[s.from][fromToNext[s.from][s.target]].distance)
							{
								if (next.type == ATTACK && enemyFactories.contains(fromToNext[i][s.target]))
								{
									// give it your all
									s.amount = troopCount[i] + (int) Math.floor(factories[i].flow);
									factories[i].flow = 0;
								}

								s.isUpgrade = false;
								troopCount[i] -= s.amount;

                        	   
                        	   int cc = (int)next.cost;
								
								if (fromToNext[i][s.target] == s.target)
								{
									next.cost -= s.amount;
								}
								if (next.cost <= 0)
								{
									actionPool.remove(next);
								}

                                s.target = fromToNext[s.from][next.target.index];
								if (!myFactories.contains(fromToNext[s.from][next.target.index]))
								{
									if (factories[fromToNext[s.from][next.target.index]].troops > cc)
									{
										s.target = next.target.index;
										System.err.println("diverted because node to strong");
									}
								}
							

								actions.add(s);

								System.err.println(i + " " + next.target.index + " " + next.cost + " " + next.reward);
								System.err.println(typeString[next.type]);

							}
						}

					}

					System.err.println("troops: " + i + " " + troopCount[i]);

					if (troopCount[i] > 10)
					{
						// send away as bomb protection
						int lowest = Integer.MAX_VALUE;
						int index = -1;

						// get closest
						for (int j = 0; j < distances.length; j++)
						{
							if (myFactories.contains(j))
							{
								if (bomb[fromToNext[i][j]] < graph[i][fromToNext[i][j]].distance && i != j)
								{
									if (distances[i][j] < lowest)
									{
										lowest = distances[i][j];
										index = j;

									}
								}
							}

						}
						if(index != -1)
						{
						    
    						// send away
    						Solve s = new Solve();
    						s.amount = troopCount[i];
    						s.from = i;
    						s.isUpgrade = false;
    						s.target = index;
    						actions.add(s);
						}
					}

				}

			}

			/*
			 * EXECUTE SOLVES!
			 */

			StringBuilder b = new StringBuilder(200);
			for (Solve s : actions)
			{
				// based on type, do shit
				if (s.isUpgrade)
				{
					// inc
					b.append("INC " + s.target + ";");

				} else
				{
					// move
					b.append("MOVE " + s.from + " " + s.target + " " + s.amount + ";");

				}
			}

			// BOMBS
			for (Integer i : enemyFactories)
			{

				int lowest = Integer.MAX_VALUE;
				int index = -1;
				if (usedBombs < 2 && bomb[i] == 0 && ((!factories[i].isDeactive && factories[i].production >= highestProd)
						|| factories[i].troops > 10 && turnCount > 5))
				{
					// get closest
					for (int j = 0; j < distances.length; j++)
					{
						if (myFactories.contains(j))
						{
							if (distances[i][j] < lowest)
							{
								lowest = distances[i][j];
								index = j;

							}
						}

					}
					b.append("BOMB " + index + " " + i);
					b.append(';');
					usedBombs++;
					bomb[i] = lowest;
					// throw bomb
				}
			}

			if (b.length() == 0)
			{
				System.out.println("WAIT");
			} else
			{
				b.setLength(b.length() - 1);
				System.out.println(b);
			}
			firstTurn = false;
		}

	}

	private static int best = 0;
	private static ArrayList<Action> bestActions;

	public static point[] dijkstra(int[][] adj, int source)
	{

		int MAXINT = Integer.MAX_VALUE;
		point[] estimates = new point[adj.length];

		// Set up our initial estimates.
		for (int i = 0; i < estimates.length; i++)
			estimates[i] = new point(MAXINT, source);

		// This estimate is 0, now.
		estimates[source].distance = 0;

		// Really, we can run this n-1 times, where n is the number of
		// vertices. The last iteration does NOT produce any different paths.
		for (int i = 0; i < estimates.length - 1; i++)
		{

			// Pick the minimal vertex to add into, S, our set of vertices
			// for which we have shortest distances.
			int vertex = 0;
			int bestseen = MAXINT;

			// In order to be chosen here, you can not have been previously
			// chosen. Also, you have to be smaller than all other candidates.
			for (int j = 0; j < estimates.length; j++)
			{
				if (estimates[j].chosen == false && estimates[j].distance < bestseen)
				{

					bestseen = estimates[j].distance;
					vertex = j;
				}
			}

			// Choose this vertex!
			estimates[vertex].chosen = true;

			// Update our estimates based on edges that leave from this vertex.
			for (int j = 0; j < estimates.length; j++)
			{

				// Do we get a shorter distance by traveling to vertex, and then
				// taking the edge from vertex to j? If so, make the update
				// here.
				if (estimates[vertex].distance + adj[vertex][j] < estimates[j].distance)
				{

					// Our new estimate to get to j, going through vertex.
					estimates[j].distance = estimates[vertex].distance + adj[vertex][j];

					// This also means that vertex is the last vertex on the new
					// shortest path to j, so we need to store this also.
					estimates[j].last = vertex;
				}
			}

		}
		for (int i = 0; i < estimates.length; i++)
		{

			point p = estimates[i];

			if (p.last == source)
			{
				// direct connection
				fromToNext[source][i] = i;

			} else
			{
				while (estimates[p.last].last != source)
				{
					p = estimates[p.last];
				}

				fromToNext[source][i] = p.last;
			}

		}

		// We return these whole estimates array.
		return estimates;
	}

	static class point
	{

		public Integer distance;
		public boolean chosen;
		public int last;

		public point(int d, int source)
		{
			distance = new Integer(d);
			last = source;
			chosen = false;
		}
	}
}
