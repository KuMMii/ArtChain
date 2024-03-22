import { useMediaQuery } from "react-responsive";
import { ReactNode } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { MainPage } from "./pages/MainPage";
import { InvestList } from "./pages/InvestList";
import { ChakraProvider } from "@chakra-ui/react";
import { LoginPage } from "./pages/LoginPage";

import { CoinCharge } from "./pages/CoinCharge";
import { CommonPage } from "./pages/CommonPage";
import { Theme } from "./theme/theme";
import Market from "./pages/Market";

import MarketDetail from "./pages/MarketDetail";
import { InvestDetail } from "./pages/InvestDetail";
import MarketTradeConfirm from "./pages/MarketTradeConfrim";
import MarketTradeNow from "./pages/MarketTradeNow";
import MarketEnroll from "./pages/MarketEnroll";
import LoginBusiness from "./pages/LoginBusiness";

import UserEnrollWithOauth from "./pages/UserEnrollWithOauth";
import UserENrollWIthNormal from "./pages/UserEnrollWithNormal";
import BusinessEnroll from "./pages/BusinessEnroll";
import UserMyPage from "./pages/UserMyPage";
import { InvestNoticeDetail } from "./pages/InvestNoticeDetail";
import BusinessMyPage from "./pages/BusinessMyPage";
import FundConfirm from "./pages/FundConfirm";
import BusinessProjectEnroll from "./pages/BusinessProjectEnroll";

function App() {
  const Desktop = ({ children }: { children: ReactNode }) => {
    const isDesktop = useMediaQuery({ minWidth: 501 });
    return isDesktop ? children : null;
  };
  const Mobile = ({ children }: { children: ReactNode }) => {
    const isMobile = useMediaQuery({ maxWidth: 500 });
    return isMobile ? children : null;
  };

  return (
    <>
      {/* 데스크탑 버전 */}
      <ChakraProvider theme={Theme}>
        <Desktop>
          <div style={{ backgroundColor: "#001a38", height: "100dvh" }}>
            <div
              style={{
                width: "390px",
                margin: "0 auto",
                backgroundColor: "white",
                height: "100dvh",
              }}
            >
              데스크탑
            </div>
          </div>
        </Desktop>
      </ChakraProvider>
      {/* 모바일 */}
      <ChakraProvider theme={Theme}>
        <Mobile>
          <div style={{ backgroundColor: "white", height: "100dvh" }}>
            {/* NavBar
          <TopNavBar /> */}
            <BrowserRouter>
              <Routes>
                {/* 메인페이지 */}
                <Route
                  path="/"
                  element={
                    <CommonPage topNavType="logo" bottomNavType="home">
                      <MainPage />
                    </CommonPage>
                  }
                />
                {/* 로그인 페이지 */}
                <Route
                  path="/login"
                  element={
                    <CommonPage topNavType="back" bottomNavType="">
                      <LoginPage />
                    </CommonPage>
                  }
                ></Route>
                <Route
                  path="/loginbusiness"
                  element={
                    <CommonPage topNavType="back" bottomNavType="">
                      <LoginBusiness />
                    </CommonPage>
                  }
                ></Route>
                {/* 회원 가입 페이지 */}
                <Route
                  path="/signupoauth"
                  element={
                    <CommonPage topNavType="back" bottomNavType="">
                      <UserEnrollWithOauth />
                    </CommonPage>
                  }
                ></Route>
                <Route
                  path="/signupnormal"
                  element={
                    <CommonPage topNavType="back" bottomNavType="">
                      <UserENrollWIthNormal />
                    </CommonPage>
                  }
                ></Route>
                <Route
                  path="/businessenroll"
                  element={
                    <CommonPage topNavType="back" bottomNavType="">
                      <BusinessEnroll />
                    </CommonPage>
                  }
                ></Route>
                {/* 코인 충전 */}
                <Route path="/coin-charge" element={<CoinCharge />}></Route>
                {/* 투자리스트 */}
                <Route
                  path="/invest-list"
                  element={
                    <CommonPage topNavType="logo" bottomNavType="invest">
                      <InvestList />
                    </CommonPage>
                  }
                />
                {/* 투자  */}
                <Route
                  path="/invest/:investId"
                  element={
                    <CommonPage topNavType="coinBack" bottomNavType="">
                      <InvestDetail />
                    </CommonPage>
                  }
                />

                {/* 작품 공지사항 */}
                <Route
                  path="/invest/:investId/notice/:noticeId"
                  element={
                    <CommonPage topNavType="back" bottomNavType="">
                      <InvestNoticeDetail />
                    </CommonPage>
                  }
                />

                {/* 마켓 */}
                <Route
                  path="/market"
                  element={
                    <CommonPage topNavType="logo" bottomNavType="market">
                      <Market />
                    </CommonPage>
                  }
                ></Route>
                <Route
                  path="/market/:id"
                  element={
                    <CommonPage topNavType="coinBack" bottomNavType="market">
                      <MarketDetail />
                    </CommonPage>
                  }
                ></Route>
                <Route
                  path="/market/tradenow/:id"
                  element={
                    <CommonPage topNavType="back" bottomNavType="button">
                      <MarketTradeNow />
                    </CommonPage>
                  }
                ></Route>
                <Route
                  path="/market/tradeconfirm/:id"
                  element={
                    <CommonPage topNavType="back" bottomNavType="market">
                      <MarketTradeConfirm />
                    </CommonPage>
                  }
                ></Route>
                <Route
                  path="/market/enroll"
                  element={
                    <CommonPage topNavType="back" bottomNavType="button">
                      <MarketEnroll />
                    </CommonPage>
                  }
                ></Route>
                {/* 마이페이지 */}
                <Route
                  path="/mypage/:id"
                  element={
                    <CommonPage topNavType="" bottomNavType="my">
                      <UserMyPage />
                    </CommonPage>
                  }
                ></Route>
                {/* 기업 마이페이지 */}
                <Route
                  path="/businesspage/:id"
                  element={
                    <CommonPage topNavType="back" bottomNavType="my">
                      <BusinessMyPage />
                    </CommonPage>
                  }
                ></Route>
                <Route
                  path="/businessconfirm/:id"
                  element={
                    <CommonPage topNavType="back" bottomNavType="">
                      <FundConfirm />
                    </CommonPage>
                  }
                ></Route>

                <Route
                  path="/businessproject"
                  element={
                    <CommonPage topNavType="back" bottomNavType="">
                      <BusinessProjectEnroll />
                    </CommonPage>
                  }
                ></Route>
              </Routes>
            </BrowserRouter>
          </div>
        </Mobile>
      </ChakraProvider>
    </>
  );
}

export default App;