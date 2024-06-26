import { Box, Center, Flex, Text } from "@chakra-ui/react";
import { useNavigate } from "react-router-dom";
import { getMarketHistoryDisplayListInterface } from "../../../type/market.interface";
import { formatNumberWithComma } from "../../Common/Comma";
import formatDate from "../../Common/Datetime";

export default function SellHistoryComponent(
  params: getMarketHistoryDisplayListInterface
) {
  const navigate = useNavigate();
  if (params.buyerAddress === null) {
    return;
  }

  return (
    <>
      {/* 판매 */}
      <Flex mt={"0.7rem"} w={"800px"}>
        <Box mr={"1.2rem"}>
          <Box
          w={"50px"}
            px={"0.6rem"}
            py={"0.1rem"}
            rounded={"0.7rem"}
            fontSize={"sm"}
            bg={"blue.300"}
          >
            <Center
              as={"b"}
              color={"white.100"}
              onClick={() => {
                navigate(`../market/tradeconfirm/${params.id}`);
              }}
            >
              보기
            </Center>
          </Box>
        </Box>

        <Center w={"4rem"}>
          <Text as={"b"} color={"blue.400"}>
            판매
          </Text>
        </Center>
        <Center w={"5rem"}>
          <Text as={"b"} color={"balck.100"}>
            {params.pieceCount}
          </Text>
        </Center>
        <Center w={"6rem"}>
          <Text as={"b"} color={"black.100"}>
            {params.coinPerPiece}
          </Text>
        </Center>
        <Center w={"6rem"}>
          <Text as={"b"} color={"black.100"}>
            {formatNumberWithComma(params.totalCoin)}
          </Text>
        </Center>
        <Center w={"6rem"}>
          <Text as={"b"} color={"black.100"}>
            {params.sellerAddress.substring(0, 6)}...
          </Text>
        </Center>
        <Center w={"7rem"}>
          <Text as={"b"} color={"black.100"}>
            {params.buyerAddress.substring(0, 6)}...
          </Text>
        </Center>
        <Center w={"12rem"}>
          <Text as={"b"} color={"black.100"}>
            {formatDate(params.historyTime)}
          </Text>
        </Center>
      </Flex>
    </>
  );
}
