# DNSresolver
Custom Domain Name System resolver. Program supports a cache to keep track of recent searches, if the query is not in the cache it is forwarded to Google DNS and cached.



![dns](https://github.com/SarahBateman22/DNSresolver/assets/142822160/f267180d-80a7-487d-ba82-bfe8ca31c267)

Example of dig to github.com routed back to my own machine. At first the cache does not contain the query so it forwards it to Google. The packet received back is stored in the cache and the next time the same address is queried the response is pulled from the cache. 
