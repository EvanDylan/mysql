CREATE DATABASE IF NOT EXISTS LocalTx DEFAULT CHARACTER SET = utf8mb4;

Use LocalTx;

CREATE TABLE `tb_order` (

  `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ProductId` int(10) unsigned NOT NULL COMMENT '商品ID',
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE `tb_product` (
  `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `qty` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

INSERT INTO Product (qty) VALUE (100);