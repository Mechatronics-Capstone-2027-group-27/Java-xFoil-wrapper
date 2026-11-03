javaaddpath('xfoil-1.0.0.jar')

try
    % initialize xfoil
    p = matz.xfoil.Xfoil(["ssh", "-tt", "this", "xfoil.exe"]);

    % automatically close xfoil when leaving this scope
    cleanup = onCleanup(@() p.close());

    % create input
    input = matz.xfoil.XfoilInput();

    % first calculation
    ar1 = p.calc(input);
    areoPrint(ar1);

    % second calculation
    input.Re = 20000;
    ar2 = p.calc(input);
    areoPrint(ar2);

catch ME
    fprintf('Error: %s\n', ME.message);
end


function areoPrint(r)
    fprintf('alpha=%.3f, CL=%.4f, Cm=%.4f, CD=%.5f, CDf=%.5f, CDp=%.5f\n', ...
        r.alpha(), r.cl(), r.cm(), r.cd(), r.cdf(), r.cdp());
end
