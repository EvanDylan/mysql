create table people (
  last_name  varchar(50) not null,
  first_name varchar(50) not null,
  dob        date not null,
  gender     enum('m', 'f') not null,
  key(last_name, first_name, dob)
);
insert into people values ('Bob', 'jos', '1989-09-11' , 'm');

  CREATE TABLE tb_2 (
    clo BINARY(3)
  ) ENGINE = 'INNODB';

  INSERT INTO tb_2 VALUES('ddd');

  EXPLAIN SELECT * FROM people WHERE first_name = 'J'


  --	生成随机字符串
  set global log_bin_trust_function_creators = 1;
  DROP FUNCTION IF EXISTS rand_string;
  DELIMITER $$
  CREATE FUNCTION rand_string(n INT)
  RETURNS VARCHAR(255)
  BEGIN
      DECLARE chars_str varchar(100) DEFAULT 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
      DECLARE return_str varchar(255) DEFAULT '';
      DECLARE i INT DEFAULT 0;
      WHILE i < n DO
          SET return_str = concat(return_str,substring(chars_str , FLOOR(1 + RAND()*62 ),1));
          SET i = i +1;
      END WHILE;
      RETURN return_str;
  END $$
  DELIMITER ;

  -- 产生随机字符串
  select rand_string(32);

  -- 创建表
  CREATE TABLE tb_3 (

  	rand_str varchar(32),
  	KEY(rand_str)

  ) ENGINE = 'INNODB';


  -- 模拟产生大表，插入数据
  DELIMITER $$
  SET AUTOCOMMIT = 0 $$
  CREATE PROCEDURE insert_data_to_tb_3 ( )
  BEGIN
  	DECLARE
  		v_cnt DECIMAL ( 10 ) DEFAULT 0;
  	dd :
  	LOOP
  			INSERT INTO tb_3
  		VALUES
  			( rand_string ( 32 ) );

  		SET v_cnt = v_cnt + 1;
  		IF
  			v_cnt = 1000000 THEN
  				LEAVE dd;

  		END IF;

  	END LOOP dd;

  END $$
  DELIMITER;

  CALL insert_data_to_tb_3;

  EXPLAIN SELECT * FROM tb_3 WHERE rand_str = 'ddd';

  -- 查询不同使用不同前缀作为索引的选择性
  SELECT COUNT(DISTINCT LEFT(rand_str, 2))/COUNT(*) as sel2,
  	COUNT(DISTINCT LEFT(rand_str, 3))/COUNT(*) as sel3 ,
  	COUNT(DISTINCT LEFT(rand_str, 4))/COUNT(*) as cel4,
  	COUNT(DISTINCT LEFT(rand_str, 5))/COUNT(*) as cel5,
  	COUNT(DISTINCT LEFT(rand_str, 6))/COUNT(*) as cel6,
  	COUNT(DISTINCT LEFT(rand_str, 7))/COUNT(*) as cel7,
  	COUNT(DISTINCT LEFT(rand_str, 8))/COUNT(*) as cel8,
  	COUNT(DISTINCT LEFT(rand_str, 9))/COUNT(*) as cel9
  FROM tb_3;

  -- 查看不同前缀数据的分步状态，如果分布的不是很均匀需要继续加长使用前缀的长度。
  SELECT COUNT(*) as cnt, LEFT(rand_str, 8) AS pref FROM tb_3 GROUP BY pref ORDER BY cnt DESC LIMIT 10;

  -- 修改tb_3表，使用前缀索引
  ALTER TABLE tb_3 DROP INDEX rand_str;
  ALTER TABLE tb_3 ADD KEY (rand_str(8));
