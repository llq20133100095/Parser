# Parser

用来调用斯坦福NLP工具的程序。[https://nlp.stanford.edu/software/lex-parser.html](https://nlp.stanford.edu/software/lex-parser.html)

## 1.stanford praser有两个不同的dependencies：
- Basic dependencies：跟普通的依赖是一样的
- Collapsed dependencies：坍塌的依赖，就是把一些in，of这些介词去掉，然后合并到一起：（可以直接指出两个词语之间的关系）

    如句子：Bell, based in Los Angeles, makes and distributes electronic, computer and building products

Stanford Dependencies (SD) representation is:

- nsubj(makes-8, Bell-1)
- nsubj(distributes-10, Bell-1)
- vmod(Bell-1, based-3)
- nn(Angeles-6, Los-5)
- prep in(based-3, Angeles-6)
- root(ROOT-0, makes-8)
- conj and(makes-8, distributes-10)
- amod(products-16, electronic-11)
- conj and(electronic-11, computer-13)
- amod(products-16, computer-13)
- conj and(electronic-11, building-15)

## 2.Train数据集中，出现的错误有：
- entity不止在句子中出现一次，所以不知道其真实位置，出现的位置为51。
- 12-<e1>pack</e1>，这种情况出现在4219。
- 有时候entity不止一个单词，取最后的那个entity。

## 3.java文件说明

（1）stanfordParser.java：主要提取出当前句子中两个单词之间的关系，比如conj and(makes-8, distributes-10)
- 输入文件：TEST_SEN_E.TXT
- 输出文件：test_segment（分词）、test_entity_position（两个实体的位置）、test_dependencies（词语间的依赖关系）、test_tagged（词语的tagged）

（2）Dijkstra.java：主要实现了dijkstra算法，然后求出两个节点的SDP（最短依赖路径）

（3）DepDijkstra.java：调用Dijkstra.java文件算法，然后求出最短路径

生成文件e1_SDP.txt,e1_sdp_pos.txt

test_sen:3666,3851,3856,3886
